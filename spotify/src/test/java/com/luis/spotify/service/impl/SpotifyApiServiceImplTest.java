package com.luis.spotify.service.impl;

import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpotifyApiServiceImplTest {
    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private UserSpotifyTokenRepository tokenRepository;

    @Mock
    private RestClient mockSpotifyApiRestClient;
    @Mock
    private RestClient mockSpotifyAuthRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private SpotifyApiServiceImpl spotifyApiService;

    private static final String CLIENT_ID = "testClientId";
    private static final String CLIENT_SECRET = "testClientSecret";
    private static final String TOKEN_URI = "https://accounts.spotify.com/api/token";
    private static final String API_URI = "https://api.spotify.com/v1";
    private static final String SPOTIFY_USER_ID = "testSpotifyUser";
    private static final String INITIAL_ACCESS_TOKEN = "initialAccessToken";
    private static final String INITIAL_REFRESH_TOKEN = "initialRefreshToken";
    private static final String NEW_ACCESS_TOKEN = "newAccessToken";
    private static final String NEW_REFRESH_TOKEN = "newRefreshToken";

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(mockSpotifyApiRestClient, mockSpotifyAuthRestClient);
        spotifyApiService = new SpotifyApiServiceImpl(restClientBuilder, tokenRepository);

        ReflectionTestUtils.setField(spotifyApiService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(spotifyApiService, "clientSecret", CLIENT_SECRET);
        ReflectionTestUtils.setField(spotifyApiService, "tokenUri", TOKEN_URI);
        ReflectionTestUtils.setField(spotifyApiService, "apiUri", API_URI);
    }

    @Test
    void givenUserWithNonExpiredSpotifyToken_whenGetUserInfo_thenReturnUserProfileWithoutTokenRefresh() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(userTokens));

        when(mockSpotifyApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(API_URI + "/me")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        SpotifyUserProfile expectedProfile = new SpotifyUserProfile();
        expectedProfile.setDisplayName("Test User Display");
        when(responseSpec.body(SpotifyUserProfile.class)).thenReturn(expectedProfile);

        // when
        SpotifyUserProfile actualProfile = spotifyApiService.getUserInfo(principal);

        // then
        assertNotNull(actualProfile);
        assertEquals("Test User Display", actualProfile.getDisplayName());

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verify(mockSpotifyApiRestClient).get();
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN);
        verify(responseSpec).body(SpotifyUserProfile.class);
    }

    @Test
    void givenUserWithExpiredSpotifyToken_whenGetUserInfo_thenRefreshTokenAndReturnUserProfile() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(userTokens));

        when(mockSpotifyAuthRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Map<String, Object> refreshTokenResponse = new HashMap<>();
        refreshTokenResponse.put("access_token", NEW_ACCESS_TOKEN);
        refreshTokenResponse.put("expires_in", 3600);
        refreshTokenResponse.put("refresh_token", NEW_REFRESH_TOKEN);
        when(responseSpec.body(Map.class)).thenReturn(refreshTokenResponse);

        when(mockSpotifyApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(API_URI + "/me")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        SpotifyUserProfile expectedProfile = new SpotifyUserProfile();
        expectedProfile.setDisplayName("Refreshed User Display");
        when(responseSpec.body(SpotifyUserProfile.class)).thenReturn(expectedProfile);

        // when
        SpotifyUserProfile actualProfile = spotifyApiService.getUserInfo(principal);

        // then
        assertNotNull(actualProfile);
        assertEquals("Refreshed User Display", actualProfile.getDisplayName());

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthRestClient).post();
        verify(tokenRepository).save(userTokens);
        assertEquals(NEW_ACCESS_TOKEN, userTokens.getAccessToken());
        assertEquals(NEW_REFRESH_TOKEN, userTokens.getRefreshToken());
        assertTrue(userTokens.getAccessTokenExpiresAt().isAfter(Instant.now()));
        verify(mockSpotifyApiRestClient).get();
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN);
    }

    @Test
    void givenNoTokensFoundForUser_whenGetUserInfo_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.empty());

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyApiService.getUserInfo(principal));
        assertEquals("No tokens found for user: " + SPOTIFY_USER_ID, thrown.getMessage());

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verifyNoInteractions(mockSpotifyApiRestClient, mockSpotifyAuthRestClient);
    }

    @Test
    void givenExpiredSpotifyTokenAndRefreshFails_whenGetUserInfo_thenPropagateRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(userTokens));

        when(mockSpotifyAuthRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Map<String, Object> refreshTokenErrorResponse = new HashMap<>();
        refreshTokenErrorResponse.put("error", "invalid_grant");
        when(responseSpec.body(Map.class)).thenReturn(refreshTokenErrorResponse);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyApiService.getUserInfo(principal));
        assertTrue(thrown.getMessage().contains("Failed to refresh Spotify Access Token"));

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthRestClient).post();
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verifyNoMoreInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenExpiredSpotifyTokenAndNullRefreshResponse_whenGetUserInfo_thenPropagateRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(userTokens));

        when(mockSpotifyAuthRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(null);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyApiService.getUserInfo(principal));
        assertTrue(thrown.getMessage().contains("Failed to refresh Spotify Access Token"));

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthRestClient).post();
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verifyNoMoreInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenUserWithExpiredSpotifyTokenAndNullRefreshToken_whenGetUserInfo_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(null);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(userTokens));

        when(mockSpotifyAuthRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_grant");
        errorResponse.put("error_description", "Invalid refresh token");
        when(responseSpec.body(Map.class)).thenReturn(errorResponse);


        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyApiService.getUserInfo(principal));

        assertTrue(thrown.getMessage().contains("Failed to refresh Spotify Access Token"));
        assertTrue(thrown.getMessage().contains(errorResponse.toString()));

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthRestClient).post();
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verifyNoMoreInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenUserWithExpiredSpotifyTokenAndRefreshResponseHasNoNewRefreshToken_whenGetUserInfo_thenRefreshTokenAndReturnUserProfile() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken(INITIAL_REFRESH_TOKEN);

        when(tokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(userTokens));

        when(mockSpotifyAuthRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Map<String, Object> refreshTokenResponse = new HashMap<>();
        refreshTokenResponse.put("access_token", NEW_ACCESS_TOKEN);
        refreshTokenResponse.put("expires_in", 3600);
        when(responseSpec.body(Map.class)).thenReturn(refreshTokenResponse);

        when(mockSpotifyApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(API_URI + "/me")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        SpotifyUserProfile expectedProfile = new SpotifyUserProfile();
        expectedProfile.setDisplayName("Refreshed User Display No New Refresh Token");
        when(responseSpec.body(SpotifyUserProfile.class)).thenReturn(expectedProfile);

        // when
        SpotifyUserProfile actualProfile = spotifyApiService.getUserInfo(principal);

        // then
        assertNotNull(actualProfile);
        assertEquals("Refreshed User Display No New Refresh Token", actualProfile.getDisplayName());

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthRestClient).post();
        verify(tokenRepository).save(userTokens);
        assertEquals(NEW_ACCESS_TOKEN, userTokens.getAccessToken());
        assertEquals(INITIAL_REFRESH_TOKEN, userTokens.getRefreshToken());
        assertTrue(userTokens.getAccessTokenExpiresAt().isAfter(Instant.now()));
        verify(mockSpotifyApiRestClient).get();
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN);
    }
}
