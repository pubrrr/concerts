package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OlympiaparkService {

    private static final String VENUE_NAME = "Olympiapark";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        RestClient restClient = RestClient.create();
        String result = restClient.get()
                .uri("https://www.olympiapark.de/api/event-list?locale=de&phrase&eventType=konzerte&genre&startDate&endDate&sort=asc&location&limit=120&page=1")
                .retrieve()
                .body(String.class);
        List<ConcertDTO> concerts = new ArrayList<>();

        if (result == null) {
            return concerts;
        }
        JsonArray hits = JsonParser.parseString(result).getAsJsonObject()
                .get("hits").getAsJsonArray();
        for (JsonElement concert : hits) {
            JsonElement source = concert.getAsJsonObject().get("_source");
            String title = source.getAsJsonObject().get("title").getAsString().trim();
            String eventType = source.getAsJsonObject().get("eventType").getAsString();
            if (!"Konzerte".equalsIgnoreCase(eventType)) {
                continue;
            }
            String location = source.getAsJsonObject().get("locationName").getAsString();
            String link = source.getAsJsonObject().get("path").getAsString();
            String dateJson = source.getAsJsonObject().get("start").getAsString();

            OffsetDateTime time = OffsetDateTime.parse(dateJson);
            LocalDate date = LocalDate.from(time);

            concerts.add(new ConcertDTO(title, date, "https://www.olympiapark.de" + link, null, location, null));
        }
        log.info("received {} {} concerts", concerts.size(), VENUE_NAME);

        return concerts;
    }

}
