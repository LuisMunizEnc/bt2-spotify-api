package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifySearchResults;
import com.luis.spotify.service.SpotifySearchApiService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpotifySearchApiControllerTest {
    @Mock
    private SpotifySearchApiService spotifySearchApiService;

    @Mock
    private Principal principal;

    @InjectMocks
    private SpotifySearchApiController spotifySearchApiController;

    private static final String TEST_QUERY = "test query";

    @Test
    void givenValidQuery_whenSearch_thenReturnOkAndSearchResults() {
        // given
        SpotifySearchResults expectedResults = new SpotifySearchResults(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        when(spotifySearchApiService.search(principal, TEST_QUERY)).thenReturn(expectedResults);

        // when
        ResponseEntity<SpotifySearchResults> response = spotifySearchApiController.search(principal, TEST_QUERY);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResults, response.getBody());

        verify(spotifySearchApiService, times(1)).search(principal, TEST_QUERY);
    }

    @Test
    void givenEmptyQuery_whenSearch_thenReturnOkAndEmptySearchResults() {
        // given
        String emptyQuery = "";
        SpotifySearchResults expectedResults = new SpotifySearchResults(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        when(spotifySearchApiService.search(principal, emptyQuery)).thenReturn(expectedResults);

        // when
        ResponseEntity<SpotifySearchResults> response = spotifySearchApiController.search(principal, emptyQuery);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResults, response.getBody());
        verify(spotifySearchApiService, times(1)).search(principal, emptyQuery);
    }

    @Test
    void givenServiceThrowsException_whenSearch_thenPropagateException() {
        // given
        RuntimeException serviceException = new RuntimeException("Spotify API error");
        when(spotifySearchApiService.search(principal, TEST_QUERY)).thenThrow(serviceException);

        // when / then
        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> spotifySearchApiController.search(principal, TEST_QUERY)
        );
        assertEquals("Spotify API error", thrown.getMessage());
        verify(spotifySearchApiService, times(1)).search(principal, TEST_QUERY);
    }
}
