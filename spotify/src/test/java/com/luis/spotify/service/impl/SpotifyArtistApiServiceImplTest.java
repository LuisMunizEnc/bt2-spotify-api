package com.luis.spotify.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis.spotify.dto.SpotifyAlbum;
import com.luis.spotify.dto.SpotifyArtist;
import com.luis.spotify.dto.SpotifyArtistPage;
import com.luis.spotify.dto.SpotifyTrack;
import com.luis.spotify.model.UserSpotifyTokens;
import com.luis.spotify.repository.UserSpotifyTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistApiServiceImplTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private UserSpotifyTokenRepository tokenRepository;

    @Mock
    private SpotifyApiServiceImpl spotifyApiService;

    @Mock
    private RestClient mockSpotifyApiRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Spy
    private ObjectMapper objectMapper;

    private SpotifyArtistApiServiceImpl spotifyArtistApiService;

    private static final String API_URI = "https://api.spotify.com/v1";
    private static final String SPOTIFY_USER_ID = "testSpotifyUser";
    private static final String INITIAL_ACCESS_TOKEN = "initialAccessToken";
    private static final String NEW_ACCESS_TOKEN = "newAccessToken";
    private static final String TEST_ARTIST_ID = "testArtistId123";

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(mockSpotifyApiRestClient);

        spotifyArtistApiService = new SpotifyArtistApiServiceImpl(
                restClientBuilder,
                spotifyApiService
        );

        ReflectionTestUtils.setField(spotifyArtistApiService, "apiUri", API_URI);
        ReflectionTestUtils.setField(spotifyArtistApiService, "objectMapper", objectMapper);
    }

    private UserSpotifyTokens createUserTokens(boolean expired) {
        UserSpotifyTokens userTokens = new UserSpotifyTokens();
        userTokens.setSpotifyUserId(SPOTIFY_USER_ID);
        userTokens.setAccessToken(INITIAL_ACCESS_TOKEN);
        userTokens.setAccessTokenExpiresAt(expired ? Instant.now().minus(1, ChronoUnit.HOURS) : Instant.now().plus(1, ChronoUnit.HOURS));
        userTokens.setRefreshToken("someRefreshToken");
        return userTokens;
    }

    @Test
    void givenUserWithNonExpiredToken_whenGetTopArtists_thenReturnListOfArtists() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/artists?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        SpotifyArtist artist1 = new SpotifyArtist("id1", "Artist One", null, null, null);
        SpotifyArtist artist2 = new SpotifyArtist("id2", "Artist Two", null, null, null);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("items", List.of(artist1, artist2));

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        List<SpotifyArtist> topArtists = spotifyArtistApiService.getTopArtists(principal);

        // then
        assertNotNull(topArtists);
        assertEquals(2, topArtists.size());
        assertEquals("Artist One", topArtists.get(0).getName());
        assertEquals("Artist Two", topArtists.get(1).getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(localRequestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN);
        verify(localRequestHeadersUriSpec, times(1)).uri(API_URI + "/me/top/artists?limit=10");
    }

    @Test
    void givenUserWithExpiredToken_whenGetTopArtists_thenRefreshAndReturnArtists() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens expiredUserTokens = createUserTokens(true);
        UserSpotifyTokens refreshedUserTokens = createUserTokens(false);
        refreshedUserTokens.setAccessToken(NEW_ACCESS_TOKEN);

        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(refreshedUserTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/artists?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        SpotifyArtist artist1 = new SpotifyArtist("id1", "Refreshed Artist", null, null, null);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("items", List.of(artist1));

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        List<SpotifyArtist> topArtists = spotifyArtistApiService.getTopArtists(principal);

        // then
        assertNotNull(topArtists);
        assertEquals(1, topArtists.size());
        assertEquals("Refreshed Artist", topArtists.get(0).getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
        verify(localRequestHeadersSpec, times(1)).header(HttpHeaders.AUTHORIZATION, "Bearer " + NEW_ACCESS_TOKEN);
    }

    @Test
    void givenNoTokensFoundForUser_whenGetTopArtists_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenThrow(new RuntimeException("No tokens found for user: " + SPOTIFY_USER_ID));

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyArtistApiService.getTopArtists(principal)
        );
        assertEquals("No tokens found for user: " + SPOTIFY_USER_ID, thrown.getMessage());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verifyNoInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenSpotifyApiReturnsNullResponseForTopArtists_whenGetTopArtists_thenReturnEmptyList() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/artists?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        when(localResponseSpec.body(Map.class)).thenReturn(null);

        // when
        List<SpotifyArtist> topArtists = spotifyArtistApiService.getTopArtists(principal);

        // then
        assertNotNull(topArtists);
        assertTrue(topArtists.isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenSpotifyApiReturnsEmptyItemsForTopArtists_whenGetTopArtists_thenReturnEmptyList() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec localRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec localRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec localResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get()).thenReturn(localRequestHeadersUriSpec);
        when(localRequestHeadersUriSpec.uri(API_URI + "/me/top/artists?limit=10")).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(localRequestHeadersSpec);
        when(localRequestHeadersSpec.retrieve()).thenReturn(localResponseSpec);

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("items", new ArrayList<>());

        when(localResponseSpec.body(Map.class)).thenReturn(apiResponse);

        // when
        List<SpotifyArtist> topArtists = spotifyArtistApiService.getTopArtists(principal);

        // then
        assertNotNull(topArtists);
        assertTrue(topArtists.isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(1)).get();
    }

    @Test
    void givenUserAndArtistId_whenGetArtistPageInfo_thenReturnFullArtistPage() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        SpotifyArtist mockArtistProfile = new SpotifyArtist(TEST_ARTIST_ID, "Artist Profile", null, null, null);
        SpotifyTrack mockTopTrack = new SpotifyTrack("track1", "Top Song", null, null, 123, null, 1);
        SpotifyAlbum mockAlbum = new SpotifyAlbum("album1", "Album Title", null, null, null, null, 5, new SpotifyTrack[]{});

        RestClient.RequestHeadersUriSpec profileUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec profileHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec profileResponseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.RequestHeadersUriSpec topTracksUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec topTracksHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec topTracksResponseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.RequestHeadersUriSpec albumsUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec albumsHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec albumsResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get())
                .thenReturn(profileUriSpec)
                .thenReturn(topTracksUriSpec)
                .thenReturn(albumsUriSpec);

        when(profileUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID)).thenReturn(profileHeadersSpec);
        when(profileHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(profileHeadersSpec);
        when(profileHeadersSpec.retrieve()).thenReturn(profileResponseSpec);
        Map<String, Object> artistProfileMap = new HashMap<>();
        artistProfileMap.put("id", mockArtistProfile.getId());
        artistProfileMap.put("name", mockArtistProfile.getName());
        when(profileResponseSpec.body(Map.class)).thenReturn(artistProfileMap);

        Map<String, Object> topTracksResponse = new HashMap<>();
        topTracksResponse.put("tracks", List.of(objectMapper.convertValue(mockTopTrack, Map.class)));
        when(topTracksUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID + "/top-tracks")).thenReturn(topTracksHeadersSpec);
        when(topTracksHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(topTracksHeadersSpec);
        when(topTracksHeadersSpec.retrieve()).thenReturn(topTracksResponseSpec);
        when(topTracksResponseSpec.body(Map.class)).thenReturn(topTracksResponse);

        Map<String, Object> albumsResponse = new HashMap<>();
        albumsResponse.put("items", List.of(objectMapper.convertValue(mockAlbum, Map.class)));
        when(albumsUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID + "/albums?limit=8")).thenReturn(albumsHeadersSpec);
        when(albumsHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(albumsHeadersSpec);
        when(albumsHeadersSpec.retrieve()).thenReturn(albumsResponseSpec);
        when(albumsResponseSpec.body(Map.class)).thenReturn(albumsResponse);

        // when
        SpotifyArtistPage artistPage = spotifyArtistApiService.getArtistPageInfo(principal, TEST_ARTIST_ID);

        // then
        assertNotNull(artistPage);
        assertNotNull(artistPage.getArtistProfile());
        assertEquals("Artist Profile", artistPage.getArtistProfile().getName());

        assertFalse(artistPage.getTopTracks().isEmpty());
        assertEquals(1, artistPage.getTopTracks().size());
        assertEquals("Top Song", artistPage.getTopTracks().get(0).getName());

        assertFalse(artistPage.getAlbums().isEmpty());
        assertEquals(1, artistPage.getAlbums().size());
        assertEquals("Album Title", artistPage.getAlbums().get(0).getName());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(3)).get();
    }

    @Test
    void givenNoTokensFoundForUser_whenGetArtistPageInfo_thenThrowRuntimeException() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenThrow(new RuntimeException("No tokens found for user: " + SPOTIFY_USER_ID));

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                spotifyArtistApiService.getArtistPageInfo(principal, TEST_ARTIST_ID)
        );
        assertEquals("No tokens found for user: " + SPOTIFY_USER_ID, thrown.getMessage());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verifyNoInteractions(mockSpotifyApiRestClient);
    }

    @Test
    void givenPartialApiResponses_whenGetArtistPageInfo_thenReturnPartialArtistPage() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);

        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        SpotifyArtist mockArtistProfile = new SpotifyArtist(TEST_ARTIST_ID, "Partial Artist", null, null, null);

        RestClient.RequestHeadersUriSpec profileUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec profileHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec profileResponseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.RequestHeadersUriSpec topTracksUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec topTracksHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec topTracksResponseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.RequestHeadersUriSpec albumsUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec albumsHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec albumsResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get())
                .thenReturn(profileUriSpec)
                .thenReturn(topTracksUriSpec)
                .thenReturn(albumsUriSpec);

        when(profileUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID)).thenReturn(profileHeadersSpec);
        when(profileHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(profileHeadersSpec);
        when(profileHeadersSpec.retrieve()).thenReturn(profileResponseSpec);
        Map<String, Object> artistProfileMap = new HashMap<>();
        artistProfileMap.put("id", mockArtistProfile.getId());
        artistProfileMap.put("name", mockArtistProfile.getName());
        when(profileResponseSpec.body(Map.class)).thenReturn(artistProfileMap);

        when(topTracksUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID + "/top-tracks")).thenReturn(topTracksHeadersSpec);
        when(topTracksHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(topTracksHeadersSpec);
        when(topTracksHeadersSpec.retrieve()).thenReturn(topTracksResponseSpec);
        when(topTracksResponseSpec.body(Map.class)).thenReturn(null);

        Map<String, Object> emptyAlbumsResponse = new HashMap<>();
        emptyAlbumsResponse.put("other_field", "data");
        when(albumsUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID + "/albums?limit=8")).thenReturn(albumsHeadersSpec);
        when(albumsHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(albumsHeadersSpec);
        when(albumsHeadersSpec.retrieve()).thenReturn(albumsResponseSpec);
        when(albumsResponseSpec.body(Map.class)).thenReturn(emptyAlbumsResponse);

        // when
        SpotifyArtistPage artistPage = spotifyArtistApiService.getArtistPageInfo(principal, TEST_ARTIST_ID);

        // then
        assertNotNull(artistPage);
        assertNotNull(artistPage.getArtistProfile());
        assertEquals("Partial Artist", artistPage.getArtistProfile().getName());

        assertTrue(artistPage.getTopTracks().isEmpty());
        assertTrue(artistPage.getAlbums().isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(3)).get();
    }

    @Test
    void givenApiCallThrowsException_whenFetchingArtistProfile_thenReturnNullProfile() {
        // given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(SPOTIFY_USER_ID);
        UserSpotifyTokens userTokens = createUserTokens(false);
        when(spotifyApiService.getAndRefreshUserToken(SPOTIFY_USER_ID)).thenReturn(userTokens);

        RestClient.RequestHeadersUriSpec profileUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec profileHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec profileResponseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.RequestHeadersUriSpec topTracksUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec topTracksHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec topTracksResponseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.RequestHeadersUriSpec albumsUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec albumsHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec albumsResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockSpotifyApiRestClient.get())
                .thenReturn(profileUriSpec)
                .thenReturn(topTracksUriSpec)
                .thenReturn(albumsUriSpec);

        when(profileUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID)).thenReturn(profileHeadersSpec);
        when(profileHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(profileHeadersSpec);
        when(profileHeadersSpec.retrieve()).thenReturn(profileResponseSpec);
        doThrow(new RuntimeException("API profile error")).when(profileResponseSpec).body(Map.class);

        Map<String, Object> topTracksSuccessResponse = Map.of("tracks", Collections.emptyList());
        when(topTracksUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID + "/top-tracks?market=US")).thenReturn(topTracksHeadersSpec);
        when(topTracksHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(topTracksHeadersSpec);
        when(topTracksHeadersSpec.retrieve()).thenReturn(topTracksResponseSpec);
        when(topTracksResponseSpec.body(Map.class)).thenReturn(topTracksSuccessResponse);

        Map<String, Object> albumsSuccessResponse = new HashMap<>();
        albumsSuccessResponse.put("items", Collections.emptyList());
        when(albumsUriSpec.uri(API_URI + "/artists/" + TEST_ARTIST_ID + "/albums?include_groups=album,single&limit=20")).thenReturn(albumsHeadersSpec);
        when(albumsHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + INITIAL_ACCESS_TOKEN)).thenReturn(albumsHeadersSpec);
        when(albumsHeadersSpec.retrieve()).thenReturn(albumsResponseSpec);
        when(albumsResponseSpec.body(Map.class)).thenReturn(albumsSuccessResponse);

        // when
        SpotifyArtistPage artistPage = spotifyArtistApiService.getArtistPageInfo(principal, TEST_ARTIST_ID);

        // then
        assertNotNull(artistPage);
        assertNull(artistPage.getArtistProfile());
        assertTrue(artistPage.getTopTracks().isEmpty());
        assertTrue(artistPage.getAlbums().isEmpty());

        verify(spotifyApiService, times(1)).getAndRefreshUserToken(SPOTIFY_USER_ID);
        verify(mockSpotifyApiRestClient, times(3)).get();
    }
}