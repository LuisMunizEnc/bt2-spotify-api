package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyUserProfile;
import com.luis.spotify.service.impl.SpotifyApiServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@Slf4j
public class SpotifyApiController {
    private final SpotifyApiServiceImpl spotifyApiServiceImpl;

    public SpotifyApiController(SpotifyApiServiceImpl spotifyApiServiceImpl) {
        this.spotifyApiServiceImpl = spotifyApiServiceImpl;
    }

    @GetMapping("/me")
    public ResponseEntity<SpotifyUserProfile> getUserInfo(Principal user) {
        log.info("Get user {} details", user.getName());
        SpotifyUserProfile profile = spotifyApiServiceImpl.getUserInfo(user);
        return ResponseEntity.ok(profile);
    }

}
