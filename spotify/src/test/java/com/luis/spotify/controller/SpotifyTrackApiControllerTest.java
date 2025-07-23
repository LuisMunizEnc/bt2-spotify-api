package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyTrack;
import com.luis.spotify.service.SpotifyTrackApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyTrackApiControllerTest {

    @Mock
    private SpotifyTrackApiService spotifyTrackApiService;

    @Mock
    private Principal principal;

    @InjectMocks
    private SpotifyTrackApiController spotifyTrackApiController;

    private static final String TEST_USER_ID = "testUser";

    @BeforeEach
    void setUp() {
        lenient().when(principal.getName()).thenReturn(TEST_USER_ID);
    }

    @Test
    void givenAuthenticatedUser_whenGetTopTracks_thenReturnOkAndListOfTracks() {
        // given
        List<SpotifyTrack> expectedTracks = Collections.singletonList(new SpotifyTrack("track1", "Top Song", null, null, 12345, null, 1));
        when(spotifyTrackApiService.getTopTracks(principal)).thenReturn(expectedTracks);

        // when
        ResponseEntity<List<SpotifyTrack>> response = spotifyTrackApiController.getTopTracks(principal);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTracks, response.getBody());
        verify(spotifyTrackApiService, times(1)).getTopTracks(principal);
    }

    @Test
    void givenAuthenticatedUser_whenGetTopTracks_thenReturnEmptyListIfNoTracks() {
        // given
        List<SpotifyTrack> expectedTracks = new ArrayList<>();
        when(spotifyTrackApiService.getTopTracks(principal)).thenReturn(expectedTracks);

        // when
        ResponseEntity<List<SpotifyTrack>> response = spotifyTrackApiController.getTopTracks(principal);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(spotifyTrackApiService, times(1)).getTopTracks(principal);
    }

    @Test
    void givenServiceThrowsException_whenGetTopTracks_thenPropagateException() {
        // given
        RuntimeException serviceException = new RuntimeException("Error fetching top tracks");
        when(spotifyTrackApiService.getTopTracks(principal)).thenThrow(serviceException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyTrackApiController.getTopTracks(principal)
        );
        assertEquals("Error fetching top tracks", thrown.getMessage());
        verify(spotifyTrackApiService, times(1)).getTopTracks(principal);
    }
}
