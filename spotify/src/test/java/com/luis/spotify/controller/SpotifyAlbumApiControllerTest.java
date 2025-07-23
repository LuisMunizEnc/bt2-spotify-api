package com.luis.spotify.controller;

import com.luis.spotify.dto.SpotifyAlbum;
import com.luis.spotify.service.SpotifyAlbumApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyAlbumApiControllerTest {

    @Mock
    private SpotifyAlbumApiService spotifyAlbumApiService;

    @Mock
    private Principal principal;

    @InjectMocks
    private SpotifyAlbumApiController spotifyAlbumApiController;

    private static final String TEST_USER_ID = "testUser";
    private static final String TEST_ALBUM_ID = "testAlbumId123";

    @BeforeEach
    void setUp() {
        lenient().when(principal.getName()).thenReturn(TEST_USER_ID);
    }

    @Test
    void givenAuthenticatedUserAndAlbumId_whenGetAlbumInfo_thenReturnOkAndAlbum() {
        // given
        SpotifyAlbum expectedAlbum = new SpotifyAlbum(TEST_ALBUM_ID, "Test Album", null, "2023-01-01", null, null, 10, null);
        when(spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID)).thenReturn(expectedAlbum);

        // when
        ResponseEntity<SpotifyAlbum> response = spotifyAlbumApiController.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAlbum, response.getBody());
        verify(spotifyAlbumApiService, times(1)).getAlbumInfo(principal, TEST_ALBUM_ID);
    }

    @Test
    void givenAlbumNotFound_whenGetAlbumInfo_thenReturnNotFound() {
        // given
        when(spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID)).thenReturn(null);

        // when
        ResponseEntity<SpotifyAlbum> response = spotifyAlbumApiController.getAlbumInfo(principal, TEST_ALBUM_ID);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(spotifyAlbumApiService, times(1)).getAlbumInfo(principal, TEST_ALBUM_ID);
    }

    @Test
    void givenServiceThrowsException_whenGetAlbumInfo_thenPropagateException() {
        // given
        RuntimeException serviceException = new RuntimeException("Error fetching album info");
        when(spotifyAlbumApiService.getAlbumInfo(principal, TEST_ALBUM_ID)).thenThrow(serviceException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyAlbumApiController.getAlbumInfo(principal, TEST_ALBUM_ID)
        );
        assertEquals("Error fetching album info", thrown.getMessage());
        verify(spotifyAlbumApiService, times(1)).getAlbumInfo(principal, TEST_ALBUM_ID);
    }
}
