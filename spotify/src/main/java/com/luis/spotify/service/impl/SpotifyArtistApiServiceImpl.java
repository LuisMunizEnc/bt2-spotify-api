package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.dto.SpotifyAlbum;
import com.luis.spotify.dto.SpotifyArtist;
import com.luis.spotify.dto.SpotifyArtistPage;
import com.luis.spotify.dto.SpotifyTrack;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.SpotifyApiService;
import com.luis.spotify.service.SpotifyArtistApiService;
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
public class SpotifyArtistApiServiceImpl implements SpotifyArtistApiService {
    private final RestClient spotifyApiRestClient;
    private final SpotifyApiServiceImpl spotifyApiService;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.provider.spotify.api-uri}")
    private String apiUri;

    public SpotifyArtistApiServiceImpl(RestClient.Builder restClientBuilder,
                                       SpotifyApiServiceImpl spotifyApiService) {
        this.spotifyApiRestClient = restClientBuilder.build();
        this.spotifyApiService = spotifyApiService;
        this.objectMapper = new ObjectMapper();
    }



    @Override
    public List<SpotifyArtist> getTopArtists(Principal principal) {
        String spotifyUserId = principal.getName();

        UserSpotifyTokens user = spotifyApiService.getAndRefreshUserToken(spotifyUserId);

        String topArtistsUri = String.format("%s/me/top/artists?limit=8", apiUri);

        Map<String, Object> responseMap = spotifyApiRestClient.get()
                .uri(topArtistsUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getAccessToken())
                .retrieve()
                .body(Map.class);

        if (responseMap == null || !responseMap.containsKey("items")) {
            log.warn("Empty or invalid response received from Spotify top artists API for user {}", spotifyUserId);
            return new ArrayList<>();
        }

        JsonNode rootNode = objectMapper.valueToTree(responseMap);
        JsonNode itemsNode = rootNode.path("items");

        List<SpotifyArtist> topArtists = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                try {
                    topArtists.add(objectMapper.treeToValue(node, SpotifyArtist.class));
                } catch (Exception e) {
                    log.error("Error mapping top artist: {}", node.toString(), e);
                }
            }
        }
        return topArtists;
    }

    @Override
    public SpotifyArtistPage getArtistPageInfo(Principal principal, String artistId) {
        String spotifyUserId = principal.getName();

        UserSpotifyTokens user = spotifyApiService.getAndRefreshUserToken(spotifyUserId);
        String accessToken = user.getAccessToken();

        SpotifyArtist artistProfile = fetchArtistProfile(accessToken, artistId);
        List<SpotifyTrack> topTracks = fetchArtistTopTracks(accessToken, artistId);
        List<SpotifyAlbum> albums = fetchArtistAlbums(accessToken, artistId);

        return new SpotifyArtistPage(artistProfile, topTracks, albums);
    }

    private SpotifyArtist fetchArtistProfile(String accessToken, String artistId) {
        log.info("Fetching artist profile for ID {}", artistId);
        try {
            String artistProfileUri = String.format("%s/artists/%s", apiUri, artistId);
            Map<String, Object> artistProfileResponse = spotifyApiRestClient.get()
                    .uri(artistProfileUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            if (artistProfileResponse != null) {
                return objectMapper.convertValue(artistProfileResponse, SpotifyArtist.class);
            } else {
                log.warn("Empty response for artist profile for ID {}", artistId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching artist profile for ID {}: {}", artistId, e.getMessage());
            return null;
        }
    }

    private List<SpotifyTrack> fetchArtistTopTracks(String accessToken, String artistId) {
        log.info("Fetching top tracks for artist ID {}", artistId);
        List<SpotifyTrack> topTracks = new ArrayList<>();
        try {
            String topTracksUri = String.format("%s/artists/%s/top-tracks", apiUri, artistId);
            Map<String, Object> topTracksResponse = spotifyApiRestClient.get()
                    .uri(topTracksUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (topTracksResponse != null && topTracksResponse.containsKey("tracks")) {
                JsonNode tracksNode = objectMapper.valueToTree(topTracksResponse).path("tracks");
                if (tracksNode.isArray()) {
                    for (JsonNode node : tracksNode) {
                        try {
                            topTracks.add(objectMapper.treeToValue(node, SpotifyTrack.class));
                        } catch (Exception e) {
                            log.error("Error mapping top track for artist {}: {}", artistId, node.toString(), e);
                        }
                    }
                }
            } else {
                log.warn("Empty or invalid response for top tracks for artist ID {}", artistId);
            }
        } catch (Exception e) {
            log.error("Error fetching top tracks for artist ID {}: {}", artistId, e.getMessage());
        }
        return topTracks;
    }

    private List<SpotifyAlbum> fetchArtistAlbums(String accessToken, String artistId) {
        log.info("Fetching albums for artist ID {}", artistId);
        List<SpotifyAlbum> albums = new ArrayList<>();
        try {
            String albumsUri = String.format("%s/artists/%s/albums?limit=8", apiUri, artistId);
            Map<String, Object> albumsResponse = spotifyApiRestClient.get()
                    .uri(albumsUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (albumsResponse != null && albumsResponse.containsKey("items")) {
                JsonNode itemsNode = objectMapper.valueToTree(albumsResponse).path("items");
                if (itemsNode.isArray()) {
                    for (JsonNode node : itemsNode) {
                        try {
                            albums.add(objectMapper.treeToValue(node, SpotifyAlbum.class));
                        } catch (Exception e) {
                            log.error("Error mapping album for artist {}: {}", artistId, node.toString(), e);
                        }
                    }
                }
            } else {
                log.warn("Empty or invalid response for albums for artist ID {}", artistId);
            }
        } catch (Exception e) {
            log.error("Error fetching albums for artist ID {}: {}", artistId, e.getMessage());
        }
        return albums;
    }
}
