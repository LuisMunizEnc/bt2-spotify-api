package com.luis.spotify.config;

import com.luis.spotify.UserSpotifyTokenRepository;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final UserSpotifyTokenRepository userSpotifyTokenRepository;

    @Value("${app.frontend.redirectUrl}")
    private String frontendRedirectUrl;

    public CustomAuthenticationSuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            UserSpotifyTokenRepository userSpotifyTokenRepository
    ){
        this.jwtTokenProvider = jwtTokenProvider;
        this.authorizedClientRepository = authorizedClientRepository;
        this.userSpotifyTokenRepository = userSpotifyTokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException
    {
        String appJwt = jwtTokenProvider.generateToken(authentication);
        if(authentication instanceof OAuth2AuthenticationToken oauth2Token){
            System.out.println(authentication);
            String clientRegistrationId = oauth2Token.getAuthorizedClientRegistrationId();

            OAuth2AuthorizedClient authorizedClient = authorizedClientRepository
                    .loadAuthorizedClient(clientRegistrationId, authentication, request);

            if(authorizedClient != null){
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

                String spotifyUserId = oauth2User.getName();
                UserSpotifyTokens userTokens = userSpotifyTokenRepository
                        .findById(spotifyUserId)
                        .orElse(new UserSpotifyTokens());

                userTokens.setSpotifyUserId(spotifyUserId);
                userTokens.setAccessToken(accessToken.getTokenValue());
                userTokens.setAccessTokenExpiresAt(accessToken.getExpiresAt());

                if (refreshToken != null) {
                    userTokens.setRefreshToken(refreshToken.getTokenValue());
                }

                userSpotifyTokenRepository.save(userTokens);

                log.info("User info to save: {}", userTokens.getSpotifyUserId());
                log.info("Spotify Access Token obtained: {}", userTokens.getAccessToken());
                if (refreshToken != null) {
                    log.info("Spotify Refresh Token obtained: {}", userTokens.getRefreshToken());
                }
                log.info("User session expiration: {}", userTokens.getAccessTokenExpiresAt());

            }

            response.sendRedirect( frontendRedirectUrl+"?token=" + appJwt);
        }
    }
}
