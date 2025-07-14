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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SpotifyApiServiceImpl implements SpotifyApiService {
    private final WebClient spotifyApiWebClient;
    private final WebClient spotifyAuthWebClient;
    private final UserSpotifyTokenRepository tokenRepository;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.spotify.token-uri}")
    private String tokenUri;

    public SpotifyApiServiceImpl(WebClient.Builder webClientBuilder, UserSpotifyTokenRepository tokenRepository){
        this.spotifyApiWebClient = webClientBuilder.baseUrl("https://api.spotify.com/v1/").build();
        this.spotifyAuthWebClient = webClientBuilder.build();
        this.tokenRepository = tokenRepository;
    }

    private UserSpotifyTokens refreshSpotifyAccessToken(UserSpotifyTokens user){
        String refreshToken = user.getRefreshToken();

        log.info("Attempting refresh with:");
        log.info("Client ID: {}", clientId);
        log.info("Client Secret: {}", clientSecret != null ? "***" : "null");
        log.info("Token URI: {}", tokenUri);
        log.info("Refresh token: {}", refreshToken);

        String authHeader = HttpHeaders.encodeBasicAuth(clientId, clientSecret, null);
        authHeader = "Basic "+ authHeader;

        Map<String,Object> response = (Map<String,Object>)(spotifyAuthWebClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block());

        log.info("Request for new token: {}", response);

        if(response != null && response.containsKey("access_token")){
            String newAccessToken = (String) response.get("access_token");
            Integer expiresIn = ((Number) response.get("expires_in")).intValue();
            String newRefreshToken = (String) response.getOrDefault("refresh_token", refreshToken);

            user.setAccessToken(newAccessToken);
            user.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
            user.setRefreshToken(newRefreshToken);
            return user;
        }else{
            throw new RuntimeException("Failed to refresh Spotify Access Token: "+ response);
        }
    }

    public SpotifyUserProfile getUserInfo(Principal principal){
        String spotifyUserId = principal.getName();
        log.info("Service: Request for user {} details", spotifyUserId);
        Optional<UserSpotifyTokens> optionalUser = tokenRepository.findById(spotifyUserId);
        UserSpotifyTokens user = optionalUser.orElseThrow(() ->
                new RuntimeException("No tokens found for user: " + spotifyUserId)
        );

        if(user.isAccessTokenExpired()){
            log.info("Token expired, requesting new one with\n clientId:{}\n clientSecret:{}\n tokenUri:{}", clientId, clientSecret, tokenUri);
            refreshSpotifyAccessToken(user);
            tokenRepository.save(user);
            log.info("Token refreshed for user {}", user.getSpotifyUserId());
        }

        return spotifyApiWebClient.get()
                .uri("/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+user.getAccessToken())
                .retrieve()
                .bodyToMono(SpotifyUserProfile.class)
                .block();
    }
}
