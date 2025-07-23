package com.luis.spotify.service;

import com.luis.spotify.dto.SpotifyTrack;

import java.security.Principal;
import java.util.List;

public interface SpotifyTrackApiService {
    List<SpotifyTrack> getTopTracks(Principal principal);
}
