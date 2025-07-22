package com.luis.spotify.service.impl;

import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.service.SpotifyApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SpotifyApiServiceImpl implements SpotifyApiService {
    private final RestClient spotifyApiRestClient;
    private final RestClient spotifyAuthRestClient;
    private final UserSpotifyTokenRepository tokenRepository;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.spotify.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.spotify.api-uri}")
    private String apiUri;

    public SpotifyApiServiceImpl(RestClient.Builder restClientBuilder, UserSpotifyTokenRepository tokenRepository){
        this.spotifyApiRestClient = restClientBuilder.build();
        this.spotifyAuthRestClient = restClientBuilder.build();
        this.tokenRepository = tokenRepository;
    }

    private UserSpotifyTokens refreshSpotifyAccessToken(UserSpotifyTokens user){
        log.info("Received token update action for {}", user.getSpotifyUserId());
        String refreshToken = user.getRefreshToken();

        String authHeader = HttpHeaders.encodeBasicAuth(clientId, clientSecret, null);
        authHeader = "Basic "+ authHeader;

        Map<String,Object> response = spotifyAuthRestClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=refresh_token&refresh_token=" + refreshToken)
                .retrieve()
                .body(Map.class);

        if(response != null && response.containsKey("access_token")){
            String newAccessToken = (String) response.get("access_token");
            Integer expiresIn = ((Number) response.get("expires_in")).intValue();
            String newRefreshToken = (String) response.getOrDefault("refresh_token", refreshToken);

            user.setAccessToken(newAccessToken);
            user.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
            user.setRefreshToken(newRefreshToken);
            return user;
        }else{
            log.info("Token refreshing for user {} failed", user.getSpotifyUserId());
            throw new RuntimeException("Failed to refresh Spotify Access Token. "+response);
        }
    }

    public SpotifyUserProfile getUserInfo(Principal principal){
        String spotifyUserId = principal.getName();
        Optional<UserSpotifyTokens> optionalUser = tokenRepository.findById(spotifyUserId);
        UserSpotifyTokens user = optionalUser.orElseThrow(() ->
                new RuntimeException("No tokens found for user: " + spotifyUserId)
        );

        if(user.isAccessTokenExpired()){
            refreshSpotifyAccessToken(user);
            tokenRepository.save(user);
            log.info("Token refreshed for user {}", user.getSpotifyUserId());
        }

        return spotifyApiRestClient.get()
                .uri(apiUri+"/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+user.getAccessToken())
                .retrieve()
                .body(SpotifyUserProfile.class);

    }
}
