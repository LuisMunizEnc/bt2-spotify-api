package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyTrack;
import com.luis.spotify.service.SpotifyTrackApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tracks")
@CrossOrigin
public class SpotifyTrackApiController {

    private final SpotifyTrackApiService spotifyTrackApiService;

    public SpotifyTrackApiController(SpotifyTrackApiService spotifyTrackApiService) {
        this.spotifyTrackApiService = spotifyTrackApiService;
    }

    @GetMapping("/top")
    public ResponseEntity<List<SpotifyTrack>> getTopTracks(Principal principal) {
        log.info("Request for top tracks received for user {}", principal.getName());
        List<SpotifyTrack> topTracks = spotifyTrackApiService.getTopTracks(principal);
        return ResponseEntity.ok(topTracks);
    }
}
