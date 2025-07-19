package com.luis.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {
    private String id;
    private String name;
    private SpotifyAlbum album;
    private SpotifyArtist[] artists;
    @JsonProperty("duration_ms") private Integer durationMs;
    @JsonProperty("external_urls") private SpotifyExternalUrl externalUrl;
    @JsonProperty("track_number") private Integer trackNumber;
}
