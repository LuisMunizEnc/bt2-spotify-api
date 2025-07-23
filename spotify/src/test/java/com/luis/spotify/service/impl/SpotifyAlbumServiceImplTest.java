package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.dto.SpotifyAlbum;
import com.luis.spotify.dto.SpotifyTrack;
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

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyAlbumApiServiceImplTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private UserSpotifyTokenRepository tokenRepository;

    @Mock
    private SpotifyApiServiceImpl spotifyApiService;

    @Mock
    private RestClient mockSpotifyApiRestClient;

    @Spy
    private ObjectMapper objectMapper;

    private SpotifyAlbumApiServiceImpl spotifyAlbumApiService;

    private static final String API_URI = "https://api.spotify.com/v1";
    private static final String SPOTIFY_USER_ID = "testUser";
    private static final String INITIAL_ACCESS_TOKEN = "initialAccessToken";
    private static final String NEW_ACCESS_TOKEN = "newAccessToken";
    private static final String TEST_ALBUM_ID = "testAlbumId123";

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(mockSpotifyApiRestClient);

        spotifyAlbumApiService = new SpotifyAlbumApiServiceImpl(
                restClientBuilder,
                tokenRepository,
                spotifyApiService
        );

        ReflectionTestUtils.setField(spotifyAlbumApiService, "apiUri", API_URI);
        ReflectionTestUtils.setField(spotifyAlbumApiService, "objectMapper", objectMapper);
    }

    private UserSpotifyTokens createUserTokens(boolean expired) {
        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(expired ? Instant.now().minus(1, ChronoUnit.HOURS) : Instant.now().plus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken("someRefreshToken");
        return userTokens;
    }

    private Map<String, Object> createSpotifyAlbumApiResponse(
            String albumId, String albumName, Integer totalTracks, List<SpotifyTrack> tracks) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", albumId);
        response.put("name", albumName);
        response.put("release_date", "2023-01-01");
        response.put("images", Collections.emptyList());
        response.put("external_urls", Map.of("spotify", "http://example.com/album"));
        response.put("artists", Collections.emptyList());
        response.put("total_tracks", totalTracks);

        Map<String, Object> tracksMap = new HashMap<>();
        List<Map<String, Object>> trackItems = new ArrayList<>();
        if (tracks != null) {
            for (SpotifyTrack track : tracks) {
                trackItems.add(objectMapper.convertValue(track, Map.class));
            }
        }
        tracksMap.put("items", trackItems);
        response.put("tracks", tracksMap);

        return response;
    }

    @Test
    void givenUserWithNonExpiredToken_whenGetAlbumInfo_thenReturnFullAlbum() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/albums/" + TEST_ALBUM_ID)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        SpotifyTrack mockTrack = new SpotifyTrack("track1", "Album Song", null, null, 12345, null, 1);
        Map<String, Object> apiResponse = createSpotifyAlbumApiResponse(
                TEST_ALBUM_ID, "My Awesome Album", 1, Collections.singletonList(mockTrack)
        );

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNotNull(album);
        assertEquals(TEST_ALBUM_ID, album.getId());
        assertEquals("My Awesome Album", album.getName());
        assertNotNull(album.getTrack());
        assertEquals(1, album.getTrack().length);
        assertEquals("Album Song", album.getTrack()[0].getName());
        assertEquals(1, album.getTotalTracks());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(localRequestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN);
    }

    @Test
    void givenUserWithExpiredToken_whenGetAlbumInfo_thenUseRefreshedTokenAndReturnAlbum() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens refreshedUserTokens = createUserTokens(false);
        refreshedUserTokens.setAccessToken(NEW_ACCESS_TOKEN);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(refreshedUserTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/albums/" + TEST_ALBUM_ID)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        SpotifyTrack mockTrack = new SpotifyTrack("track2", "Refreshed Album Song", null, null, 45678, null, 2);
        Map<String, Object> apiResponse = createSpotifyAlbumApiResponse(
                TEST_ALBUM_ID, "Refreshed Album", 1, Collections.singletonList(mockTrack)
        );

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNotNull(album);
        assertEquals("Refreshed Album", album.getName());
        assertEquals(1, album.getTrack().length);
        assertEquals("Refreshed Album Song", album.getTrack()[0].getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(localRequestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN);
    }

    @Test
    void givenNoTokensFoundForUser_whenGetAlbumInfo_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenThrow(new RuntimeException("No tokens found for user: " + SPOTIFY_USER_ID));

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID)
        );
        assertEquals("No tokens found for user: " + SPOTIFY_USER_ID, thrown.getMessage());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verifyNoInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenSpotifyApiReturnsNullResponse_whenGetAlbumInfo_thenReturnNull() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/albums/" + TEST_ALBUM_ID)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        when(localResponseSpec.body(Map.class)).thenReturn(null);

        // when
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNull(album);
        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenSpotifyApiReturnsAlbumWithoutTracksNode_whenGetAlbumInfo_thenReturnAlbumWithEmptyTracks() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/albums/" + TEST_ALBUM_ID)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("id", TEST_ALBUM_ID);
        apiResponse.put("name", "Album No Tracks");
        apiResponse.put("release_date", "2023-01-01");
        apiResponse.put("images", Collections.emptyList());
        apiResponse.put("external_urls", Map.of("spotify", "http://example.com/album"));
        apiResponse.put("artists", Collections.emptyList());
        apiResponse.put("total_tracks", 0);

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNotNull(album);
        assertEquals("Album No Tracks", album.getName());
        assertNotNull(album.getTrack());
        assertEquals(0, album.getTrack().length);
        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenSpotifyApiReturnsAlbumWithEmptyTracksItems_whenGetAlbumInfo_thenReturnAlbumWithEmptyTracks() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/albums/" + TEST_ALBUM_ID)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        Map<String, Object> apiResponse = createSpotifyAlbumApiResponse(
                TEST_ALBUM_ID, "Album Empty Tracks", 0, Collections.emptyList()
        );

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNotNull(album);
        assertEquals("Album Empty Tracks", album.getName());
        assertNotNull(album.getTrack());
        assertEquals(0, album.getTrack().length);
        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenApiCallThrowsException_whenGetAlbumInfo_thenReturnNull() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/albums/" + TEST_ALBUM_ID)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        doThrow(new RuntimeException("API album error")).when(localResponseSpec).body(Map.class);

        // when
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNull(album);
        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }
}
