package com.luis.spotify.service;

import com.luis.spotify.dto.SpotifySearchResults;

import java.security.Principal;

public interface SpotifySearchApiService {
    SpotifySearchResults search(Principal principal, String query);
}
