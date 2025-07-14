package com.luis.spotify.service.impl;

import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.SpotifyApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    private WebClient.Builder webClientBuilder;
    @Mock
    private UserSpotifyTokenRepository tokenRepository;
    @Mock
    private WebClient mockSpotifyApiWebClient;
    @Mock
    private WebClient mockSpotifyAuthWebClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private Mono<SpotifyUserProfile> spotifyUserProfileMono;
    @Mock
    private Mono<Map> mapMono;

    private SpotifyApiServiceImpl spotifyApiService;

    private static final String CLIENT_ID = "testClientId";
    private static final String CLIENT_SECRET = "testClientSecret";
    private static final String TOKEN_URI = "https://accounts.spotify.com/api/token";
    private static final String SPOTIFY_USER_ID = "testSpotifyUser";
    private static final String INITIAL_ACCESS_TOKEN = "initialAccessToken";
    private static final String INITIAL_REFRESH_TOKEN = "initialRefreshToken";
    private static final String NEW_ACCESS_TOKEN = "newAccessToken";
    private static final String NEW_REFRESH_TOKEN = "newRefreshToken";

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockSpotifyApiWebClient, mockSpotifyAuthWebClient);

        spotifyApiService = new SpotifyApiServiceImpl(webClientBuilder, tokenRepository);

        ReflectionTestUtils.setField(spotifyApiService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(spotifyApiService, "clientSecret", CLIENT_SECRET);
        ReflectionTestUtils.setField(spotifyApiService, "tokenUri", TOKEN_URI);
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

        when(mockSpotifyApiWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/me")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SpotifyUserProfile.class)).thenReturn(spotifyUserProfileMono);

        SpotifyUserProfile expectedProfile = new SpotifyUserProfile();
        expectedProfile.setDisplayName("Test User Display");
        when(spotifyUserProfileMono.block()).thenReturn(expectedProfile);

        // when
        SpotifyUserProfile actualProfile = spotifyApiService.getUserInfo(principal);

        // then
        assertNotNull(actualProfile);
        assertEquals("Test User Display", actualProfile.getDisplayName());

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verify(mockSpotifyApiWebClient).get();
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN);
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

        when(mockSpotifyAuthWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mapMono);

        Map<String, Object> refreshTokenResponse = new HashMap<>();
        refreshTokenResponse.put("access_token", NEW_ACCESS_TOKEN);
        refreshTokenResponse.put("expires_in", 3600);
        refreshTokenResponse.put("refresh_token", NEW_REFRESH_TOKEN);
        when(mapMono.block()).thenReturn(refreshTokenResponse);

        when(mockSpotifyApiWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/me")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SpotifyUserProfile.class)).thenReturn(spotifyUserProfileMono);

        SpotifyUserProfile expectedProfile = new SpotifyUserProfile();
        expectedProfile.setDisplayName("Refreshed User Display");
        when(spotifyUserProfileMono.block()).thenReturn(expectedProfile);

        // when
        SpotifyUserProfile actualProfile = spotifyApiService.getUserInfo(principal);

        // then
        assertNotNull(actualProfile);
        assertEquals("Refreshed User Display", actualProfile.getDisplayName());

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthWebClient).post();
        verify(tokenRepository).save(userTokens);
        assertEquals(NEW_ACCESS_TOKEN, userTokens.getAccessToken());
        assertEquals(NEW_REFRESH_TOKEN, userTokens.getRefreshToken());
        assertTrue(userTokens.getAccessTokenExpiresAt().isAfter(Instant.now()));
        verify(mockSpotifyApiWebClient).get();
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
        verifyNoInteractions(mockSpotifyApiWebClient, mockSpotifyAuthWebClient);
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

        when(mockSpotifyAuthWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mapMono);

        Map<String, Object> refreshTokenErrorResponse = new HashMap<>();
        refreshTokenErrorResponse.put("error", "invalid_grant");
        when(mapMono.block()).thenReturn(refreshTokenErrorResponse);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                        spotifyApiService.getUserInfo(principal));
        assertTrue(thrown.getMessage().contains("Failed to refresh Spotify Access Token"));

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthWebClient).post();
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verifyNoMoreInteractions(mockSpotifyApiWebClient);
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

        when(mockSpotifyAuthWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(mapMono);
        when(mapMono.block()).thenReturn(null);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                        spotifyApiService.getUserInfo(principal));
        assertTrue(thrown.getMessage().contains("Failed to refresh Spotify Access Token"), "El mensaje de error debe indicar fallo en el refresh");

        verify(tokenRepository).findById(SPOTIFY_USER_ID);
        verify(mockSpotifyAuthWebClient).post();
        verify(tokenRepository, never()).save(any(UserSpotifyTokens.class));
        verifyNoMoreInteractions(mockSpotifyApiWebClient);
    }
}
