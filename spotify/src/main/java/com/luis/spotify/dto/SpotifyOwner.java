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
public class SpotifyOwner {
    private String id;
    @JsonProperty("display_name") private String displayName;
    @JsonProperty("external_urls") private SpotifyExternalUrl externalUrl;
}
