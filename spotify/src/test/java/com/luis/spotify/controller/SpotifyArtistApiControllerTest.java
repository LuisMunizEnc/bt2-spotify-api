package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyArtist;
import com.luis.spotify.dto.SpotifyArtistPage;
import com.luis.spotify.service.SpotifyArtistApiService;
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
class SpotifyArtistApiControllerTest {

    @Mock
    private SpotifyArtistApiService spotifyArtistApiService;

    @Mock
    private Principal principal;

    @InjectMocks
    private SpotifyArtistApiController spotifyArtistApiController;

    private static final String TEST_USER_ID = "testUser";
    private static final String TEST_ARTIST_ID = "testArtistId123";

    @BeforeEach
    void setUp() {
        lenient().when(principal.getName()).thenReturn(TEST_USER_ID);
    }

    @Test
    void givenAuthenticatedUser_whenGetTopArtists_thenReturnOkAndListOfArtists() {
        // given
        List<SpotifyArtist> expectedArtists = Collections.singletonList(new SpotifyArtist("artist1", "Artist Name", null, null, null));
        when(spotifyArtistApiService.getTopArtists(principal)).thenReturn(expectedArtists);

        // when
        ResponseEntity<List<SpotifyArtist>> response = spotifyArtistApiController.getTopArtists(principal);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedArtists, response.getBody());
        verify(spotifyArtistApiService, times(1)).getTopArtists(principal);
    }

    @Test
    void givenAuthenticatedUser_whenGetTopArtists_thenReturnEmptyListIfNoArtists() {
        // given
        List<SpotifyArtist> expectedArtists = new ArrayList<>();
        when(spotifyArtistApiService.getTopArtists(principal)).thenReturn(expectedArtists);

        // when
        ResponseEntity<List<SpotifyArtist>> response = spotifyArtistApiController.getTopArtists(principal);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(spotifyArtistApiService, times(1)).getTopArtists(principal);
    }

    @Test
    void givenServiceThrowsException_whenGetTopArtists_thenPropagateException() {
        // given
        RuntimeException serviceException = new RuntimeException("Error fetching top artists");
        when(spotifyArtistApiService.getTopArtists(principal)).thenThrow(serviceException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyArtistApiController.getTopArtists(principal)
        );
        assertEquals("Error fetching top artists", thrown.getMessage());
        verify(spotifyArtistApiService, times(1)).getTopArtists(principal);
    }

    @Test
    void givenAuthenticatedUserAndArtistId_whenGetArtistPageInfo_thenReturnOkAndArtistPage() {
        // given
        SpotifyArtistPage expectedArtistPage = new SpotifyArtistPage(
                new SpotifyArtist("artistProfileId", "Profile Artist", null, null, null),
                Collections.emptyList(),
                Collections.emptyList()
        );
        when(spotifyArtistApiService.getArtistPageInfo(principal, TEST_ARTIST_ID)).thenReturn(expectedArtistPage);

        // when
        ResponseEntity<SpotifyArtistPage> response = spotifyArtistApiController.getArtistPageInfo(principal, TEST_ARTIST_ID);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedArtistPage, response.getBody());
        verify(spotifyArtistApiService, times(1)).getArtistPageInfo(principal, TEST_ARTIST_ID);
    }

    @Test
    void givenServiceThrowsException_whenGetArtistPageInfo_thenPropagateException() {
        // given
        RuntimeException serviceException = new RuntimeException("Error fetching artist page info");
        when(spotifyArtistApiService.getArtistPageInfo(principal, TEST_ARTIST_ID)).thenThrow(serviceException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyArtistApiController.getArtistPageInfo(principal, TEST_ARTIST_ID)
        );
        assertEquals("Error fetching artist page info", thrown.getMessage());
        verify(spotifyArtistApiService, times(1)).getArtistPageInfo(principal, TEST_ARTIST_ID);
    }
}

