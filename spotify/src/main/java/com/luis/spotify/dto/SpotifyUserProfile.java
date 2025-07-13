package com.luis.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyUserProfile {
    private String id;
    @JsonProperty("display_name") private String displayName;
    private String email;
    private String country;
    private SpotifyImage[] images;
    private String product;
    private String uri;
}

