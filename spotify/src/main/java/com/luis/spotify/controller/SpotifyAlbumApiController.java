package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyAlbum;
import com.luis.spotify.service.SpotifyAlbumApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/albums")
@CrossOrigin
public class SpotifyAlbumApiController {

    private final SpotifyAlbumApiService spotifyAlbumApiService;

    public SpotifyAlbumApiController(SpotifyAlbumApiService spotifyAlbumApiService) {
        this.spotifyAlbumApiService = spotifyAlbumApiService;
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<SpotifyAlbum> getAlbumInfo(
            Principal principal,
            @PathVariable String albumId
    ) {
        log.info("Controller: Request for album info received for user {} and album ID {}", principal.getName(), albumId);
        SpotifyAlbum album = spotifyAlbumApiService.getAlbumInfo(principal, albumId);
        if (album == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(album);
    }
}

