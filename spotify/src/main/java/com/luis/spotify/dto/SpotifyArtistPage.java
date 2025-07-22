package com.luis.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtistPage {
    private SpotifyArtist artistProfile;
    private List<SpotifyTrack> topTracks;
    private List<SpotifyAlbum> albums;
}

