package com.luis.spotify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifySearchResults {
    private List<SpotifyTrack> tracks;
    private List<SpotifyAlbum> albums;
    private List<SpotifyArtist> artists;
    private List<SpotifySearchPlaylist> playlists;
}
