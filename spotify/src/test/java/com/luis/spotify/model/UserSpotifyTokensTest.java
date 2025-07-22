package com.luis.spotify.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class UserSpotifyTokensTest {
    @Test
    void givenFutureExpiration_whenIsAccessTokenExpired_thenReturnFalse() {
        // given
        UserSpotifyTokens userTokens = UserSpotifyTokens.builder()
                .spotifyUserId("testUser")
                .accessToken("someAccessToken")
                .accessTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .refreshToken("someRefreshToken")
                .build();

        // when
        boolean isExpired = userTokens.isAccessTokenExpired();

        // then
        assertFalse(isExpired);
    }

    @Test
    void givenPastExpiration_whenIsAccessTokenExpired_thenReturnTrue() {
        // given
        UserSpotifyTokens userTokens = UserSpotifyTokens.builder()
                .spotifyUserId("testUser")
                .accessToken("someAccessToken")
                .accessTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .refreshToken("someRefreshToken")
                .build();

        // when
        boolean isExpired = userTokens.isAccessTokenExpired();

        // then
        assertTrue(isExpired);
    }

    @Test
    void givenNullExpiration_whenIsAccessTokenExpired_thenReturnFalse() {
        // given
        UserSpotifyTokens userTokens = UserSpotifyTokens.builder()
                .spotifyUserId("testUser")
                .accessToken("someAccessToken")
                .accessTokenExpiresAt(null)
                .refreshToken("someRefreshToken")
                .build();

        // when
        boolean isExpired = userTokens.isAccessTokenExpired();

        // then
        assertFalse(isExpired);
    }

}
