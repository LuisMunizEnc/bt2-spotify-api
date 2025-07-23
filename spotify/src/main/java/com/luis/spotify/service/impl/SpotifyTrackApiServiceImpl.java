package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.dto.SpotifyTrack;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.SpotifyApiService;
import com.luis.spotify.service.SpotifyTrackApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpotifyTrackApiServiceImpl implements SpotifyTrackApiService {
    private final RestClient spotifyApiRestClient;
    private final SpotifyApiServiceImpl spotifyApiService;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.provider.spotify.api-uri}")
    private String apiUri;

    public SpotifyTrackApiServiceImpl(RestClient.Builder restClientBuilder,
                                      UserSpotifyTokenRepository tokenRepository,
                                      SpotifyApiServiceImpl spotifyApiService) {
        this.spotifyApiRestClient = restClientBuilder.build();
        this.spotifyApiService = spotifyApiService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<SpotifyTrack> getTopTracks(Principal principal) {
        String spotifyUserId = principal.getName();

        UserSpotifyTokens user = spotifyApiService.getAndRefreshUserToken(spotifyUserId);
        String accessToken = user.getAccessToken();

        String topTracksUri = String.format("%s/me/top/tracks?limit=10", apiUri);

        Map<String, Object> responseMap = spotifyApiRestClient.get()
                .uri(topTracksUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        if (responseMap == null || !responseMap.containsKey("items")) {
            log.warn("Empty or invalid response received from Spotify top tracks API for user {}", spotifyUserId);
            return new ArrayList<>();
        }

        JsonNode rootNode = objectMapper.valueToTree(responseMap);
        JsonNode itemsNode = rootNode.path("items");

        List<SpotifyTrack> topTracks = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                try {
                    topTracks.add(objectMapper.treeToValue(node, SpotifyTrack.class));
                } catch (Exception e) {
                    log.error("Error mapping top track: {}", node.toString(), e);
                }
            }
        }
        return topTracks;
    }
}
