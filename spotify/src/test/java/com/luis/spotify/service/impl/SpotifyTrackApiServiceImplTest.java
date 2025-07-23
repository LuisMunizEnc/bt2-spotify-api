package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class SpotifyTrackApiServiceImplTest {

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

    private SpotifyTrackApiServiceImpl spotifyTrackApiService;

    private static final String API_URI = "https://api.spotify.com/v1";
    private static final String SPOTIFY_USER_ID = "testSpotifyUser";
    private static final String INITIAL_ACCESS_TOKEN = "initialAccessToken";
    private static final String NEW_ACCESS_TOKEN = "newAccessToken";

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(mockSpotifyApiRestClient);

        spotifyTrackApiService = new SpotifyTrackApiServiceImpl(
                restClientBuilder,
                tokenRepository,
                spotifyApiService
        );

        ReflectionTestUtils.setField(spotifyTrackApiService, "apiUri", API_URI);
        ReflectionTestUtils.setField(spotifyTrackApiService, "objectMapper", objectMapper);
    }

    private UserSpotifyTokens createUserTokens(boolean expired) {
        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(expired ? Instant.now().minus(1, ChronoUnit.HOURS) : Instant.now().plus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken("someRefreshToken");
        return userTokens;
    }

    @Test
    void givenUserWithNonExpiredToken_whenGetTopTracks_thenReturnListOfTracks() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/tracks?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        SpotifyTrack track1 = new SpotifyTrack("id1", "Track One", null, null, 12345, null, 1);
        SpotifyTrack track2 = new SpotifyTrack("id2", "Track Two", null, null, 23456, null, 2);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("items", List.of(track1, track2));

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        List<SpotifyTrack> topTracks = spotifyTrackApiService.getTopTracks(principal);

        // then
        assertNotNull(topTracks);
        assertEquals(2, topTracks.size());
        assertEquals("Track One", topTracks.get(0).getName());
        assertEquals("Track Two", topTracks.get(1).getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(localRequestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN);
        verify(localRequestHeadersUriSpec, times(1)).uri(API_URI + "/me/top/tracks?limit=10");
    }

    @Test
    void givenUserWithExpiredToken_whenGetTopTracks_thenUseRefreshedTokenAndReturnTracks() {
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
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/tracks?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        SpotifyTrack track1 = new SpotifyTrack("id3", "Refreshed Track", null, null, 34567, null, 3);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("items", List.of(track1));

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        List<SpotifyTrack> topTracks = spotifyTrackApiService.getTopTracks(principal);

        // then
        assertNotNull(topTracks);
        assertEquals(1, topTracks.size());
        assertEquals("Refreshed Track", topTracks.get(0).getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(localRequestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN);
    }

    @Test
    void givenNoTokensFoundForUser_whenGetTopTracks_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenThrow(new RuntimeException("No tokens found for user: " + SPOTIFY_USER_ID));

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyTrackApiService.getTopTracks(principal)
        );
        assertEquals("No tokens found for user: " + SPOTIFY_USER_ID, thrown.getMessage());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verifyNoInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenSpotifyApiReturnsNullResponseForTopTracks_whenGetTopTracks_thenReturnEmptyList() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/tracks?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        when(localResponseSpec.body(Map.class)).thenReturn(null);

        // when
        List<SpotifyTrack> topTracks = spotifyTrackApiService.getTopTracks(principal);

        // then
        assertNotNull(topTracks);
        assertTrue(topTracks.isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenSpotifyApiReturnsEmptyItemsForTopTracks_whenGetTopTracks_thenReturnEmptyList() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/tracks?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("items", new ArrayList<>());

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        List<SpotifyTrack> topTracks = spotifyTrackApiService.getTopTracks(principal);

        // then
        assertNotNull(topTracks);
        assertTrue(topTracks.isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }
    
}
