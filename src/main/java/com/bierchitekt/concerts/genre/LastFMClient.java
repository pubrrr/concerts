package com.bierchitekt.concerts.genre;

import com.bierchitekt.concerts.venues.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class LastFMClient {

    RestClient restClient = RestClient.create();


    @Value("${lastfm.apikey}")
    @NotEmpty
    private String apiKey;

    public Set<String> getGenres(String artist) {
        try {
            String escapedArtist = StringEscapeUtils.escapeHtml4(artist);
            String result = restClient.get()
                    .uri("https://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" + escapedArtist +
                            "&api_key=" + apiKey + "&format=json")
                    .retrieve()
                    .body(String.class);

            JsonElement jsonElement = JsonParser.parseString(result).getAsJsonObject()
                    .get("artist").getAsJsonObject()
                    .get("tags").getAsJsonObject()
                    .get("tag");
            JsonArray asJsonArray = jsonElement.getAsJsonArray();

            Set<String> genres = new HashSet<>();
            for (int i = 0; i < asJsonArray.size(); i++) {
                genres.add(StringUtil.capitalizeWords(asJsonArray.get(i).getAsJsonObject().get("name").getAsString()));
            }
            return genres;
        } catch (Exception ex) {
            log.warn("error getting artist {} from lastFM", artist);
            return Set.of();
        }
    }

}
