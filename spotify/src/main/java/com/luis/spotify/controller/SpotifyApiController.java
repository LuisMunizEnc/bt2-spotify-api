package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.service.SpotifyApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@Slf4j
public class SpotifyApiController {
    @Autowired
    private SpotifyApiService spotifyApiService;

    @GetMapping("/me")
    public ResponseEntity<SpotifyUserProfile> getUserInfo(Principal user) {
        log.info("Get user {} details", user.getName());
        SpotifyUserProfile profile = spotifyApiService.getUserInfo(user);
        return ResponseEntity.ok(profile);
    }

}
