package com.luis.spotify.service;

import com.luis.spotify.dto.SpotifyAlbum;

import java.security.Principal;

public interface SpotifyAlbumApiService {
    SpotifyAlbum getAlbumInfo(Principal principal, String albumId);
}
