package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifySearchResults;
import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.service.SpotifySearchApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SpotifySearchApiController {

    private final SpotifySearchApiService spotifySearchApiService;

    public SpotifySearchApiController(SpotifySearchApiService spotifySearchApiService) {
        this.spotifySearchApiService = spotifySearchApiService;
    }

    @GetMapping
    public ResponseEntity<SpotifySearchResults> search(
            Principal user,
            @RequestParam String q
    ){
        log.info("Search request received with query '{}' ", q);
        SpotifySearchResults results = spotifySearchApiService.search(user, q);
        return ResponseEntity.ok(results);
    }
}
