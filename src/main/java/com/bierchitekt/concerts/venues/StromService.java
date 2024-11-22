package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.bierchitekt.concerts.venues.XmlUtils.extractXpath;
import static com.bierchitekt.concerts.venues.XmlUtils.getDocument;

@Slf4j
@Service
@RequiredArgsConstructor
public class StromService {

    private static final String URL = "https://strom-muc.de/";

    @SuppressWarnings("java:S1192")
    public List<ConcertDTO> getConcerts() {
        List<ConcertDTO> concerts = new ArrayList<>();

        try {
            log.info("getting Strom concerts");
            Document doc = getDocument(URL);
            for (int i = 3; i < 99; i++) {
                String xpathTitle = "/html/body/div/div[2]/div[1]/div/section/div/div[3]/div/div[" + i + "]/div/h3/a";
                String xpathLink = "/html/body/div/div[2]/div[1]/div/section/div/div[3]/div/div[" + i + "]/div/h3/a/@href";
                String xpathType = "/html/body/div/div[2]/div[1]/div/section/div/div[3]/div/div[" + i + "]/div/div[2]/a";

                String type = extractXpath(xpathType, doc);

                if (!"konzert".equalsIgnoreCase(type)) {
                    continue;
                }
                String title = extractXpath(xpathTitle, doc);

                title = StringEscapeUtils.unescapeHtml4(title);
                String link = extractXpath(xpathLink, doc);

                getDate(link);
                ConcertDTO concertDTO = new ConcertDTO(title, null, link, null, "Strom", null);
                concerts.add(concertDTO);
            }

        } catch (Exception ex) {
            log.error("ex", ex);
            return List.of();
        }
        return concerts;
    }

    @SuppressWarnings("java:S1075")
    public LocalDate getDate(String link) {
        try {
            Document detailDoc = getDocument(link);

            String xpathDate = "/html/body/div/div[2]/div[1]/div/div/div[1]/div[1]/div/div[1]/div[1]/div[2]/div[1]";

            String date = extractXpath(xpathDate, detailDoc).trim();
            date = StringUtils.remove(date, "Datum:").trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate localDate = LocalDate.MIN;
            if (StringUtils.isNotEmpty(date)) {
                return LocalDate.parse(date, formatter);
            }
            return localDate;
        } catch (Exception ex) {
            log.warn("exception", ex);
        }
        return LocalDate.MIN;

    }
}
