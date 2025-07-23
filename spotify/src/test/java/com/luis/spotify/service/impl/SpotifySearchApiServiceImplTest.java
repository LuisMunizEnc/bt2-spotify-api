package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import com.luis.spotify.dto.*;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpotifySearchApiServiceImplTest {
    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private UserSpotifyTokenRepository tokenRepository;

    @Mock
    private SpotifyApiServiceImpl spotifyApiService;

    @Mock
    private RestClient mockSpotifyApiRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Spy
    private ObjectMapper objectMapper;

    private SpotifySearchApiServiceImpl spotifySearchApiService;

    private static final String API_URI = "https://api.spotify.com/v1";
    private static final String SPOTIFY_USER_ID = "testSpotifyUser";
    private static final String INITIAL_ACCESS_TOKEN = "initialAccessToken";
    private static final String INITIAL_REFRESH_TOKEN = "initialRefreshToken";
    private static final String NEW_ACCESS_TOKEN = "newAccessToken";
    private static final String TEST_QUERY = "test song";

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(mockSpotifyApiRestClient);

        spotifySearchApiService = new SpotifySearchApiServiceImpl(
                restClientBuilder,
                tokenRepository,
                spotifyApiService
        );

        ReflectionTestUtils.setField(spotifySearchApiService, "apiUri", API_URI);
        ReflectionTestUtils.setField(spotifySearchApiService, "objectMapper", objectMapper);


        lenient().when(mockSpotifyApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private UserSpotifyTokens createUserTokens(boolean expired) {
        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(expired ? Instant.now().minus(1, ChronoUnit.HOURS) : Instant.now().plus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);
        return userTokens;
    }

    private Map<String, Object> createSpotifyApiResponse(
            List<SpotifyTrack> tracks,
            List<SpotifyAlbum> albums,
            List<SpotifyArtist> artists,
            List<SpotifySearchPlaylist> playlists) {

        Map<String, Object> response = new HashMap<>();

        Map<String, Object> tracksMap = new HashMap<>();
        tracksMap.put("items", tracks);
        response.put("tracks", tracksMap);

        Map<String, Object> albumsMap = new HashMap<>();
        albumsMap.put("items", albums);
        response.put("albums", albumsMap);

        Map<String, Object> artistsMap = new HashMap<>();
        artistsMap.put("items", artists);
        response.put("artists", artistsMap);

        Map<String, Object> playlistsMap = new HashMap<>();
        playlistsMap.put("items", playlists);
        response.put("playlists", playlistsMap);

        return response;
    }

    @Test
    void givenUserWithNonExpiredToken_whenSearch_thenReturnSearchResultsWithoutTokenRefresh() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        SpotifyTrack track = new SpotifyTrack("id1", "Track Name", null, null, 12345, null, 1);
        SpotifyAlbum album = new SpotifyAlbum("idA", "Album Name", null, "2023-01-01", null, null, null,null);
        SpotifyArtist artist = new SpotifyArtist("idAr", "Artist Name", null, null, null);
        SpotifySearchPlaylist playlist = new SpotifySearchPlaylist("idP", "Playlist Name", null, "Desc", null, null, new SpotifyTracksPlaylist("href", 5));

        Map<String, Object> apiResponse = createSpotifyApiResponse(
                Collections.singletonList(track),
                Collections.singletonList(album),
                Collections.singletonList(artist),
                Collections.singletonList(playlist)
        );

        when(responseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifySearchResults results = spotifySearchApiService.search(principal, TEST_QUERY);

        // then
        assertNotNull(results);
        assertFalse(results.getTracks().isEmpty());
        assertEquals(1, results.getTracks().size());
        assertEquals("Track Name", results.getTracks().get(0).getName());

        assertFalse(results.getAlbums().isEmpty());
        assertEquals(1, results.getAlbums().size());
        assertEquals("Album Name", results.getAlbums().get(0).getName());

        assertFalse(results.getArtists().isEmpty());
        assertEquals(1, results.getArtists().size());
        assertEquals("Artist Name", results.getArtists().get(0).getName());

        assertFalse(results.getPlaylists().isEmpty());
        assertEquals(1, results.getPlaylists().size());
        assertEquals("Playlist Name", results.getPlaylists().get(0).getName());
        assertEquals(5, results.getPlaylists().get(0).getTracks().getTotal());


        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(spotifyApiService, never()).refreshSpotifyAccessToken(any(UserSpotifyTokens.class));
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(requestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN);
    }

    @Test
    void givenUserWithExpiredToken_whenSearch_thenRefreshTokenAndReturnSearchResults() {
        // given
            Principal principal = mock(Principal.class);
            when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

            UserSpotifyTokens refreshedUserTokens = new UserSpotifyTokens();
            refreshedUserTokens.setSpotifyUserId(SPOTIFY_USER_ID);
            refreshedUserTokens.setAccessToken(NEW_ACCESS_TOKEN);
            refreshedUserTokens.setAccessTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            refreshedUserTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);

            when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(refreshedUserTokens);

            Map<String, Object> apiResponse = createSpotifyApiResponse(
                Collections.singletonList(new SpotifyTrack("id2", "Refreshed Track", null, null, 123, null, 1)),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );
        when(responseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifySearchResults results = spotifySearchApiService.search(principal, TEST_QUERY);

        // then
        assertNotNull(results);
        assertFalse(results.getTracks().isEmpty());
        assertEquals("Refreshed Track", results.getTracks().get(0).getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(requestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN);
    }

    @Test
    void givenNoTokensFoundForUser_whenSearch_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenThrow(
                new RuntimeException("No tokens found for user: " + SPOTIFY_USER_ID)
        );

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifySearchApiService.search(principal, TEST_QUERY)
        );
        assertEquals("No tokens found for user: " + SPOTIFY_USER_ID, thrown.getMessage());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verifyNoInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenSpotifyApiReturnsNullResponse_whenSearch_thenReturnEmptySearchResults() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        when(responseSpec.body(Map.class)).thenReturn(null);

        // when
        SpotifySearchResults results = spotifySearchApiService.search(principal, TEST_QUERY);

        // then
        assertNotNull(results);
        assertTrue(results.getTracks().isEmpty());
        assertTrue(results.getAlbums().isEmpty());
        assertTrue(results.getArtists().isEmpty());
        assertTrue(results.getPlaylists().isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenSpotifyApiReturnsEmptyResponse_whenSearch_thenReturnEmptySearchResults() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        when(responseSpec.body(Map.class)).thenReturn(new HashMap<>());

        // when
        SpotifySearchResults results = spotifySearchApiService.search(principal, TEST_QUERY);

        // then
        assertNotNull(results);
        assertTrue(results.getTracks().isEmpty());
        assertTrue(results.getAlbums().isEmpty());
        assertTrue(results.getArtists().isEmpty());
        assertTrue(results.getPlaylists().isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenSpotifyApiResponseWithPartialData_whenSearch_thenMapAvailableDataCorrectly() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> tracksMap = new HashMap<>();
        tracksMap.put("items", Collections.singletonList(new SpotifyTrack("t1", "Track A", null, null, 100, null, 1)));
        apiResponse.put("tracks", tracksMap);

        Map<String, Object> artistsMap = new HashMap<>();
        artistsMap.put("items", Collections.singletonList(new SpotifyArtist("a1", "Artist B", null, null, null)));
        apiResponse.put("artists", artistsMap);

        when(responseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifySearchResults results = spotifySearchApiService.search(principal, TEST_QUERY);

        // then
        assertNotNull(results);
        assertEquals(1, results.getTracks().size());
        assertEquals("Track A", results.getTracks().get(0).getName());
        assertEquals(0, results.getAlbums().size());
        assertEquals(1, results.getArtists().size());
        assertEquals("Artist B", results.getArtists().get(0).getName());
        assertEquals(0, results.getPlaylists().size()); 
    }
}
