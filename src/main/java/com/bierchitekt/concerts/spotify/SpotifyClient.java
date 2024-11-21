package com.bierchitekt.concerts.spotify;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Service
@Slf4j
public class SpotifyClient {


    @Value("${spotify.clientId}")
    @NotEmpty
    private String clientId;

    @Value("${spotify.clientSecret}")
    @NotEmpty
    private String clientSecret;

    private SpotifyResponse spotifyResponse;

    RestClient restClient = RestClient.create();


    public List<String> getGenres(String artist) {

        String accessToken = getAccessToken();

        if(StringUtils.isEmpty(accessToken)){
            return List.of();
        }

        String escapedArtist = artist.replace(" ", "+");
        String result = restClient.get()
                .uri("https://api.spotify.com/v1/search?q=" + escapedArtist + "&type=artist")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
        if (StringUtils.isEmpty(result)) {
            return List.of();
        }

        JsonArray asJsonArray = JsonParser.parseString(result).getAsJsonObject()
                .get("artists").getAsJsonObject()
                .get("items").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("genres")
                .getAsJsonArray();

        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<String> genres = new Gson().fromJson(asJsonArray, listType);
        log.info("got genres {} for artist {}", genres, artist);
        return new Gson().fromJson(asJsonArray, listType);
    }

    private String getAccessToken() {

        if (spotifyResponse != null && spotifyResponse.expiresAt().isAfter(LocalDateTime.now())) {
            return spotifyResponse.accessToken();
        }

        String result = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .contentType(APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .body(String.class);
        if (StringUtils.isEmpty(result)) {
            return "";
        }
        String accessToken = JsonParser.parseString(result).getAsJsonObject()
                .get("access_token").getAsString();
        spotifyResponse = new SpotifyResponse(accessToken, LocalDateTime.now().plusHours(59));
        return accessToken;
    }

}
