package com.luis.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyPlaylist {
    private String id;
    private String name;
    private SpotifyOwner owner;
    private String description;
    private SpotifyImage[] images;
    @JsonProperty("external_urls") private SpotifyExternalUrl externalUrl;
    private SpotifyTrack[] tracks;
}
