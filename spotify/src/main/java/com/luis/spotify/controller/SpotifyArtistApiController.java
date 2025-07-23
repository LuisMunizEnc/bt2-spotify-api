package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyArtist;
import com.luis.spotify.dto.SpotifyArtistPage;
import com.luis.spotify.service.SpotifyArtistApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/artists")
@Slf4j
@CrossOrigin
public class SpotifyArtistApiController {
    private final SpotifyArtistApiService spotifyArtistApiService;

    public SpotifyArtistApiController(SpotifyArtistApiService spotifyArtistApiService) {
        this.spotifyArtistApiService = spotifyArtistApiService;
    }

    @GetMapping("/top")
    public ResponseEntity<List<SpotifyArtist>> getTopArtists(Principal principal) {
        log.info("Request for top artists received for user {}", principal.getName());
        List<SpotifyArtist> topArtists = spotifyArtistApiService.getTopArtists(principal);
        return ResponseEntity.ok(topArtists);
    }

    @GetMapping("/{artistId}")
    public ResponseEntity<SpotifyArtistPage> getArtistPageInfo(
            Principal principal,
            @PathVariable String artistId
    ) {
        log.info("Request for artist page info received for user {} and artist ID {}", principal.getName(), artistId);
        SpotifyArtistPage artistPageInfo = spotifyArtistApiService.getArtistPageInfo(principal, artistId);
        return ResponseEntity.ok(artistPageInfo);
    }
}
