package com.bierchitekt.concerts.genre;

import java.time.LocalDateTime;

public record SpotifyResponse(
        String accessToken,
        LocalDateTime expiresAt) {
}
