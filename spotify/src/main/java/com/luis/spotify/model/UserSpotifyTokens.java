package com.luis.spotify.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSpotifyTokens {
    @Id
    private String spotifyUserId;
    @Column(length = 2048)
    private String accessToken;
    private Instant accessTokenExpiresAt;
    @Column(length = 2048)
    private String refreshToken;

    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt != null && accessTokenExpiresAt.isBefore(Instant.now());
    }
}
