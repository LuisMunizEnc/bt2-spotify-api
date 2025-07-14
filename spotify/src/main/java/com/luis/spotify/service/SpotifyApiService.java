package com.luis.spotify.service;

import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.model.UserSpotifyTokens;

import java.security.Principal;

public interface SpotifyApiService {
    SpotifyUserProfile getUserInfo(Principal principal);
}
