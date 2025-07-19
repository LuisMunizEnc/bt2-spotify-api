package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.dto.*;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.SpotifySearchApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SpotifySearchApiServiceImpl implements SpotifySearchApiService {
    private final RestClient spotifyApiRestClient;
    private final UserSpotifyTokenRepository tokenRepository;
    private final SpotifyApiServiceImpl spotifyApiService;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.provider.spotify.api-uri}")
    private String apiUri;

    public SpotifySearchApiServiceImpl(RestClient.Builder restClientBuilder,
                                       UserSpotifyTokenRepository tokenRepository,
                                       SpotifyApiServiceImpl spotifyApiService) {
        this.spotifyApiRestClient = restClientBuilder.build();
        this.tokenRepository = tokenRepository;
        this.spotifyApiService = spotifyApiService;
        this.objectMapper = new ObjectMapper();
    }

    private SpotifySearchResults mapSearchResults(Map<String, Object> responseMap) {
        JsonNode rootNode = objectMapper.valueToTree(responseMap);

        List<SpotifyTrack> tracks = new ArrayList<>();
        List<SpotifyAlbum> albums = new ArrayList<>();
        List<SpotifyArtist> artists = new ArrayList<>();
        List<SpotifySearchPlaylist> playlists = new ArrayList<>();

        JsonNode tracksNode = rootNode.path("tracks").path("items");
        if (tracksNode.isArray()) {
            for (JsonNode node : tracksNode) {
                try {
                    tracks.add(objectMapper.treeToValue(node, SpotifyTrack.class));
                } catch (Exception e) {
                    log.error("Error mapping track: {}", node.toString(), e);
                }
            }
        }

        JsonNode albumsNode = rootNode.path("albums").path("items");
        if (albumsNode.isArray()) {
            for (JsonNode node : albumsNode) {
                try {
                    albums.add(objectMapper.treeToValue(node, SpotifyAlbum.class));
                } catch (Exception e) {
                    log.error("Error mapping album: {}", node.toString(), e);
                }
            }
        }

        JsonNode artistsNode = rootNode.path("artists").path("items");
        if (artistsNode.isArray()) {
            for (JsonNode node : artistsNode) {
                try {
                    artists.add(objectMapper.treeToValue(node, SpotifyArtist.class));
                } catch (Exception e) {
                    log.error("Error mapping artist: {}", node.toString(), e);
                }
            }
        }

        JsonNode playlistsNode = rootNode.path("playlists").path("items");
        if (playlistsNode.isArray()) {
            for (JsonNode playlistNode : playlistsNode) {
                try {
                    SpotifySearchPlaylist playlist = objectMapper.treeToValue(playlistNode, SpotifySearchPlaylist.class);
                    if(playlist != null) playlists.add(playlist);
                } catch (Exception e) {
                    log.error("Error mapping playlist: {}", playlistNode.toString(), e);
                }
            }
        }

        return new SpotifySearchResults(tracks, albums, artists, playlists);
    }

    @Override
    public SpotifySearchResults search(Principal principal, String query) {
        String spotifyUserId = principal.getName();
        String types = "album,track,playlist,artist";

        Optional<UserSpotifyTokens> optionalUser = tokenRepository.findById(spotifyUserId);
        UserSpotifyTokens user = optionalUser.orElseThrow(() ->
                new RuntimeException("No tokens found for user: " + spotifyUserId)
        );

        if (user.isAccessTokenExpired()) {
            spotifyApiService.refreshSpotifyAccessToken(user);
            tokenRepository.save(user);
            log.info("Token refreshed and saved for user {}", spotifyUserId);
        }

        String searchUri = String.format("%s/search?q=%s&type=%s&limit=5", apiUri, query, types);

        Map<String, Object> responseMap = spotifyApiRestClient.get()
                .uri(searchUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getAccessToken())
                .retrieve()
                .body(Map.class);

        if (responseMap == null) {
            log.warn("Empty response received from Spotify search API for user {}", spotifyUserId);
            return new SpotifySearchResults(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        return mapSearchResults(responseMap);
    }
}
