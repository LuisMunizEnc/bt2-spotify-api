package com.luis.spotify.config;

import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.service.impl.JwtTokenProviderServiceImpl;
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
    private final JwtTokenProviderServiceImpl jwtTokenProviderServiceImpl;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final UserSpotifyTokenRepository userSpotifyTokenRepository;

    @Value("${app.frontend.redirectUrl}")
    private String frontendRedirectUrl;

    public CustomAuthenticationSuccessHandler(
            JwtTokenProviderServiceImpl jwtTokenProviderServiceImpl,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            UserSpotifyTokenRepository userSpotifyTokenRepository
    ){
        this.jwtTokenProviderServiceImpl = jwtTokenProviderServiceImpl;
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
        if(authentication instanceof OAuth2AuthenticationToken oauth2Token){
            String appJwt = jwtTokenProviderServiceImpl.generateToken(authentication);
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

                log.info("User {} saved tokens", userTokens.getSpotifyUserId());
            }

            response.sendRedirect( frontendRedirectUrl+"?token=" + appJwt);
        }else log.info("Authentication different from OAuth");
    }
}
