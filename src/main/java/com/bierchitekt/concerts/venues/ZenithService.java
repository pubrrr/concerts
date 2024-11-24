package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.bierchitekt.concerts.venues.XmlUtils.extractXpath;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZenithService {

    String url = "https://muenchen.motorworld.de/";

    public List<ConcertDTO> getConcerts() {
        try {

            org.w3c.dom.Document doc = XmlUtils.getDocument(url);
            List<ConcertDTO> concerts = new ArrayList<>();
            for (int i = 1; i < 99; i++) {
                String xpathTitle = "/html/body/main/div/div/div[6]/div/div/div[1]/div/div/div[" + i + "]/a/div/div[2]/div/div[1]/div/h1";
                String xpathDate = "/html/body/main/div/div/div[6]/div/div/div[1]/div/div/div[" + i + "]/a/div/div[3]/div/div/div";
                String xpathLink = "/html/body/main/div/div/div[6]/div/div/div[1]/div/div/div[" + i + "]/a/@href";
                String title = extractXpath(xpathTitle, doc);
                if (title.isEmpty()) {
                    break;
                }
                title = StringEscapeUtils.unescapeHtml4(title);
                String date = extractXpath(xpathDate, doc).trim();
                String link = extractXpath(xpathLink, doc);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                LocalDate localDate = LocalDate.parse(date, formatter);

                ConcertDTO concertDTO = new ConcertDTO(title, localDate, link, null, "zenith", null);
                log.info("title {} date {}", title, date);
                concerts.add(concertDTO);
            }

            return concerts;
        } catch (Exception ex) {
            return List.of();
        }
    }
}
