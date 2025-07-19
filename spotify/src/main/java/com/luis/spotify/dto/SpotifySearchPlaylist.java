package com.luis.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySearchPlaylist {
    private String id;
    private String name;
    private SpotifyOwner owner;
    private String description;
    private SpotifyImage[] images;
    @JsonProperty("external_urls") private SpotifyExternalUrl externalUrl;
    private SpotifyTracksPlaylist tracks;
}
