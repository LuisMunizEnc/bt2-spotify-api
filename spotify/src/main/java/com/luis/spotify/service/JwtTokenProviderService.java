package com.luis.spotify.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

public interface JwtTokenProviderService {
    String generateToken(Authentication authentication);

    boolean validateToken(String authToken);

    String getUserIdFromJWT(String token);

    Claims getAllClaimsFromJWT(String token);
}
