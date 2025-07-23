package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.dto.SpotifyAlbum;
import com.luis.spotify.dto.SpotifyTrack;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.SpotifyAlbumApiService;
import com.luis.spotify.service.SpotifyApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SpotifyAlbumApiServiceImpl implements SpotifyAlbumApiService {

    private final RestClient spotifyApiRestClient;
    private final SpotifyApiService spotifyApiService;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.provider.spotify.api-uri}")
    private String apiUri;

    public SpotifyAlbumApiServiceImpl(RestClient.Builder restClientBuilder,
                                      UserSpotifyTokenRepository tokenRepository,
                                      SpotifyApiService spotifyApiService) {
        this.spotifyApiRestClient = restClientBuilder.build();
        this.spotifyApiService = spotifyApiService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public SpotifyAlbum getAlbumInfo(Principal principal, String albumId) {
        String spotifyUserId = principal.getName();
        log.info("Service: Request for album info for user {} and album ID {}", spotifyUserId, albumId);

        UserSpotifyTokens user = spotifyApiService.getAndRefreshUserToken(spotifyUserId);
        String accessToken = user.getAccessToken();

        String albumUri = String.format("%s/albums/%s", apiUri, albumId);

        try {
            Map<String, Object> responseMap = spotifyApiRestClient.get()
                    .uri(albumUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (responseMap == null) {
                log.warn("Empty response received from Spotify album API for ID {}", albumId);
                return null;
            }

            SpotifyAlbum album = objectMapper.convertValue(responseMap, SpotifyAlbum.class);

            JsonNode rootNode = objectMapper.valueToTree(responseMap);
            JsonNode tracksNode = rootNode.path("tracks").path("items");

            List<SpotifyTrack> albumTracks = new ArrayList<>();
            if (tracksNode.isArray()) {
                for (JsonNode node : tracksNode) {
                    try {
                        albumTracks.add(objectMapper.treeToValue(node, SpotifyTrack.class));
                    } catch (Exception e) {
                        log.error("Error mapping track for album {}: {}", albumId, node.toString(), e);
                    }
                }
            }
            album.setTrack(albumTracks.toArray(new SpotifyTrack[0]));

            return album;

        } catch (Exception e) {
            log.error("Error fetching album info for ID {}: {}", albumId, e.getMessage());
            return null;
        }
    }
}
