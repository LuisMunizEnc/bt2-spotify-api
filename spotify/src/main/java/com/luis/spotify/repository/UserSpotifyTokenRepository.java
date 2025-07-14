package com.luis.spotify.repository;

import com.luis.spotify.model.UserSpotifyTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSpotifyTokenRepository extends JpaRepository<UserSpotifyTokens, String> {
}
