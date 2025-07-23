package com.luis.spotify.service;

import com.luis.spotify.dto.SpotifyArtist;
import com.luis.spotify.dto.SpotifyArtistPage;

import java.security.Principal;
import java.util.List;

public interface SpotifyArtistApiService {
    List<SpotifyArtist> getTopArtists(Principal principal);

    SpotifyArtistPage getArtistPageInfo(Principal principal, String artistId);
}
