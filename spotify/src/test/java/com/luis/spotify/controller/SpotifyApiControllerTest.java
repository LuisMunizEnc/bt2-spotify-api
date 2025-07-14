package com.luis.spotify.controller;

import com.luis.spotify.config.CustomAuthenticationSuccessHandler;
import com.luis.spotify.config.SecurityConfig;
import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import com.luis.spotify.service.impl.JwtTokenProviderServiceImpl;
import com.luis.spotify.service.impl.SpotifyApiServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(SpotifyApiController.class)
@Import(SecurityConfig.class)
public class SpotifyApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpotifyApiServiceImpl spotifyApiService;

    @MockitoBean
    private JwtTokenProviderServiceImpl jwtTokenProvider;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @MockitoBean
    private OAuth2AuthorizedClientRepository authorizedClientRepository;
    @MockitoBean
    private UserSpotifyTokenRepository userSpotifyTokenRepository;

    @Test
    @WithMockUser(username = "testUser")
    void givenAuthenticatedUser_whenGetUserInfo_thenReturnUserProfileAndHttp200() throws Exception {
        // given
        SpotifyUserProfile expectedProfile = new SpotifyUserProfile();
        expectedProfile.setDisplayName("Test User Display Name");
        expectedProfile.setId("testUserId123");
        expectedProfile.setEmail("test@example.com");
        expectedProfile.setCountry("US");
        expectedProfile.setProduct("premium");
        expectedProfile.setUri("spotify:user:testUserId123");

        when(spotifyApiService.getUserInfo(any(Principal.class))).thenReturn(expectedProfile);

        // when
        mockMvc.perform(get("/me")
                        .principal(() -> "testUser")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.display_name").value("Test User Display Name"))
                .andExpect(jsonPath("$.id").value("testUserId123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.country").value("US"))
                .andExpect(jsonPath("$.product").value("premium"));
    }
}
