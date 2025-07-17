package com.luis.spotify.config;

import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.impl.JwtTokenProviderServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomAuthenticationSuccessHandlerTest {
    @Mock
    private JwtTokenProviderServiceImpl jwtTokenProviderService;

    @Mock
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Mock
    private UserSpotifyTokenRepository userSpotifyTokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private static final String FRONTEND_REDIRECT_URL = "http://localhost:3000/callback";
    private static final String SPOTIFY_USER_ID = "spotifyUser123";
    private static final String SPOTIFY_DISPLAY_NAME = "Test User";
    private static final String CLIENT_REGISTRATION_ID = "spotify";
    private static final String MOCKED_JWT = "mocked.jwt.token";
    private static final String SPOTIFY_ACCESS_TOKEN_VALUE = "spotifyAccessToken";
    private static final String SPOTIFY_REFRESH_TOKEN_VALUE = "spotifyRefreshToken";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customAuthenticationSuccessHandler, "frontendRedirectUrl", FRONTEND_REDIRECT_URL);
    }

    private OAuth2AuthenticationToken createOAuth2AuthenticationToken(OAuth2User oauth2User) {
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        lenient().when(clientRegistration.getRegistrationId()).thenReturn(CLIENT_REGISTRATION_ID);
        return new OAuth2AuthenticationToken(oauth2User, Collections.emptyList(), CLIENT_REGISTRATION_ID);
    }

    private OAuth2User createOAuth2User() {
        OAuth2User oauth2User = mock(OAuth2User.class);
        lenient().when(oauth2User.getName()).thenReturn(CustomAuthenticationSuccessHandlerTest.SPOTIFY_USER_ID);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("display_name", CustomAuthenticationSuccessHandlerTest.SPOTIFY_DISPLAY_NAME);
        lenient().when(oauth2User.getAttributes()).thenReturn(attributes);
        return oauth2User;
    }

    private OAuth2AuthorizedClient createAuthorizedClient(String refreshTokenValue) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                CustomAuthenticationSuccessHandlerTest.SPOTIFY_ACCESS_TOKEN_VALUE,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        OAuth2RefreshToken refreshToken = (refreshTokenValue != null) ? new OAuth2RefreshToken(refreshTokenValue, null) : null;

        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        lenient().when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        lenient().when(authorizedClient.getRefreshToken()).thenReturn(refreshToken);
        return authorizedClient;
    }

    @Test
    void givenOAuth2AuthenticationAndNewUser_whenOnAuthenticationSuccess_thenSaveTokensAndRedirect() throws IOException {
        // given
        OAuth2User oauth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createOAuth2AuthenticationToken(oauth2User);

        OAuth2AuthorizedClient authorizedClient = createAuthorizedClient(SPOTIFY_REFRESH_TOKEN_VALUE);

        when(jwtTokenProviderService.generateToken(authentication)).thenReturn(MOCKED_JWT);
        when(authorizedClientRepository.loadAuthorizedClient(eq(CLIENT_REGISTRATION_ID), eq(authentication), eq(request)))
                .thenReturn(authorizedClient);
        when(userSpotifyTokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.empty());

        // when
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(jwtTokenProviderService).generateToken(authentication);
        verify(authorizedClientRepository).loadAuthorizedClient(CLIENT_REGISTRATION_ID, authentication, request);
        verify(userSpotifyTokenRepository).findById(SPOTIFY_USER_ID);

        verify(userSpotifyTokenRepository).save(any(UserSpotifyTokens.class));
        verify(response).sendRedirect(FRONTEND_REDIRECT_URL + "?token=" + MOCKED_JWT);
    }

    @Test
    void givenOAuth2AuthenticationAndExistingUser_whenOnAuthenticationSuccess_thenUpdateTokensAndRedirect() throws IOException {
        // given
        OAuth2User oauth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createOAuth2AuthenticationToken(oauth2User);

        OAuth2AuthorizedClient authorizedClient = createAuthorizedClient(SPOTIFY_REFRESH_TOKEN_VALUE);

        UserSpotifyTokens existingUserTokens = new UserSpotifyTokens();
        existingUserTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        existingUserTokens.setAccessToken("oldAccessToken");
        existingUserTokens.setRefreshToken("oldRefreshToken");
        existingUserTokens.setAccessTokenExpiresAt(Instant.now().minusSeconds(1000));

        when(jwtTokenProviderService.generateToken(authentication)).thenReturn(MOCKED_JWT);
        when(authorizedClientRepository.loadAuthorizedClient(eq(CLIENT_REGISTRATION_ID), eq(authentication), eq(request)))
                .thenReturn(authorizedClient);
        when(userSpotifyTokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.of(existingUserTokens));

        // when
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(jwtTokenProviderService).generateToken(authentication);
        verify(authorizedClientRepository).loadAuthorizedClient(CLIENT_REGISTRATION_ID, authentication, request);
        verify(userSpotifyTokenRepository).findById(SPOTIFY_USER_ID);

        verify(userSpotifyTokenRepository).save(existingUserTokens);
        assertEquals(SPOTIFY_ACCESS_TOKEN_VALUE, existingUserTokens.getAccessToken());
        assertEquals(SPOTIFY_REFRESH_TOKEN_VALUE, existingUserTokens.getRefreshToken());
        assertTrue(existingUserTokens.getAccessTokenExpiresAt().isAfter(Instant.now().minusSeconds(1)));

        verify(response).sendRedirect(FRONTEND_REDIRECT_URL + "?token=" + MOCKED_JWT);
    }

    @Test
    void givenOAuth2AuthenticationAndNullRefreshToken_whenOnAuthenticationSuccess_thenSaveTokensWithoutRefreshTokenAndRedirect() throws IOException {
        // given
        OAuth2User oauth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createOAuth2AuthenticationToken(oauth2User);

        OAuth2AuthorizedClient authorizedClient = createAuthorizedClient(null);

        when(jwtTokenProviderService.generateToken(authentication)).thenReturn(MOCKED_JWT);
        when(authorizedClientRepository.loadAuthorizedClient(eq(CLIENT_REGISTRATION_ID), eq(authentication), eq(request)))
                .thenReturn(authorizedClient);
        when(userSpotifyTokenRepository.findById(SPOTIFY_USER_ID)).thenReturn(Optional.empty());

        // when
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(jwtTokenProviderService).generateToken(authentication);
        verify(authorizedClientRepository).loadAuthorizedClient(CLIENT_REGISTRATION_ID, authentication, request);
        verify(userSpotifyTokenRepository).findById(SPOTIFY_USER_ID);

        verify(userSpotifyTokenRepository).save(any(UserSpotifyTokens.class));
        verify(response).sendRedirect(FRONTEND_REDIRECT_URL + "?token=" + MOCKED_JWT);
    }

    @Test
    void givenOAuth2AuthenticationAndNullAuthorizedClient_whenOnAuthenticationSuccess_thenDoNotSaveTokensAndRedirect() throws IOException {
        // given
        OAuth2User oauth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createOAuth2AuthenticationToken(oauth2User);

        when(jwtTokenProviderService.generateToken(authentication)).thenReturn(MOCKED_JWT);
        when(authorizedClientRepository.loadAuthorizedClient(eq(CLIENT_REGISTRATION_ID), eq(authentication), eq(request)))
                .thenReturn(null);

        // when
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(jwtTokenProviderService).generateToken(authentication);
        verify(authorizedClientRepository).loadAuthorizedClient(CLIENT_REGISTRATION_ID, authentication, request);
        verifyNoInteractions(userSpotifyTokenRepository);
        verify(response).sendRedirect(FRONTEND_REDIRECT_URL + "?token=" + MOCKED_JWT);
    }

    @Test
    void givenNonOAuth2Authentication_whenOnAuthenticationSuccess_thenDoNothingAndLogInfo() throws IOException {
        // given
        Authentication authentication = mock(Authentication.class, withSettings().lenient());
        when(authentication.getPrincipal()).thenReturn("someUser");

        // when
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verifyNoInteractions(jwtTokenProviderService);
        verifyNoInteractions(authorizedClientRepository);
        verifyNoInteractions(userSpotifyTokenRepository);
        verifyNoInteractions(response);
    }

    @Test
    void givenOAuth2AuthenticationAndJwtGenerationFails_whenOnAuthenticationSuccess_thenPropagateExceptionAndDoNotSaveTokens() throws IOException {
        // given
        OAuth2User oauth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createOAuth2AuthenticationToken(oauth2User);

        when(jwtTokenProviderService.generateToken(authentication)).thenThrow(new RuntimeException("Error generating JWT"));

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        );

        assertEquals("Error generating JWT", thrown.getMessage());

        verify(jwtTokenProviderService).generateToken(authentication);
        verifyNoInteractions(authorizedClientRepository);
        verifyNoInteractions(userSpotifyTokenRepository);
        verifyNoInteractions(response);
    }
}
