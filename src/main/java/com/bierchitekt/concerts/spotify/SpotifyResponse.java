package com.bierchitekt.concerts.spotify;

import java.time.LocalDateTime;

public record SpotifyResponse(
        String accessToken,
        LocalDateTime expiresAt) {
}
