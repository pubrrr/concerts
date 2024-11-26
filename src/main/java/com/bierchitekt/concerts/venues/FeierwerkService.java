package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FeierwerkService {

    private static final String VENUE_NAME = "Feierwerk";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {}} concerts", VENUE_NAME);
        List<ConcertDTO> allConcerts = new ArrayList<>();


        String baseUrl = "https://www.feierwerk.de/";
        String feierwerkDirectory = "/tmp/feierwerk/";
        Set<String> strings = listFilesUsingJavaIO(feierwerkDirectory);
        log.info("downloaded {} {} pages", strings.size(), VENUE_NAME);

        for (String file : strings) {

            File input = new File(feierwerkDirectory + file);
            Document doc;
            try {
                doc = Jsoup.parse(input, "UTF-8", "https://www.feierwerk.de/");
            } catch (IOException e) {
                return List.of();
            }

            Elements concerts = doc.select("div.event.cp-view");
            for (Element concert : concerts) {
                String title = "";
                String concertDetail = "";
                Element titleElement = concert.select("h2.event-artist-name").first();
                if (titleElement != null) {
                    concertDetail = titleElement.text();
                } else {
                    continue;
                }

                // result: » The Peach Cans [ Funk, Disco, Jazz … | München ]
                title = StringUtils.substringBetween(concertDetail, "»", "[").trim();

                String genre = StringUtils.substringBetween(concertDetail, "[", "…").trim();

                if (genre.contains("Illustrationen")) {
                    continue;
                }

                Set<String> genres = Arrays.stream(genre.split(",")).collect(Collectors.toSet());
                String select2 = concert.select("div.event-date-location").first().text();
                String link = baseUrl + concert.select("a[href]").getFirst().attr("href");

                String substring = select2.substring(3, 13);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate localDate = LocalDate.parse(substring, formatter);

                ConcertDTO concertDTO = new ConcertDTO(title, localDate, link, genres, VENUE_NAME, null);
                allConcerts.add(concertDTO);
            }

        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
        return allConcerts;
    }

    private Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }
}
