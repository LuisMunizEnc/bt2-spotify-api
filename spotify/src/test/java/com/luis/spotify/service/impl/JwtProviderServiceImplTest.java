package com.luis.spotify.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtProviderServiceImplTest {
    @InjectMocks
    private JwtTokenProviderServiceImpl jwtTokenProvider;

    @BeforeEach
    void setup(){
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "thisisalongsecretkeyforjwttokenproviderthatisatleast256bitslong");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    void givenValidOAuth2Authentication_whenGenerateToken_thenReturnValidJwtTokenWithCorrectClaims(){
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("spotifyUserId123");
        when(oAuth2User.getAttribute("display_name")).thenReturn("Test User");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // when
        String token = jwtTokenProvider.generateToken(authentication);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));

        Claims claims = jwtTokenProvider.getAllClaimsFromJWT(token);
        assertEquals("spotifyUserId123", claims.get("userId"));
        assertEquals("Test User", claims.get("userName"));
        assertEquals("Test User", claims.getSubject());
        assertTrue(claims.getIssuedAt().before(new Date()));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void givenValidJwtToken_whenValidateToken_thenReturnTrue() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("testUser456");
        when(oAuth2User.getAttribute("display_name")).thenReturn("Another User");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        String validToken = jwtTokenProvider.generateToken(authentication);

        // when
        boolean isValid = jwtTokenProvider.validateToken(validToken);

        // then
        assertTrue(isValid);
    }

    @Test
    void givenExpiredJwtToken_whenValidateToken_thenReturnFalse() {
        // given
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 1);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("expiredUser");
        when(oAuth2User.getAttribute("display_name")).thenReturn("Expired Test User");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        String expiredToken = jwtTokenProvider.generateToken(authentication);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertFalse(isValid);
    }

    @Test
    void givenInvalidSignatureJwtToken_whenValidateToken_thenReturnFalse() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("signatureUser");
        when(oAuth2User.getAttribute("display_name")).thenReturn("Signature Test User");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        String validToken = jwtTokenProvider.generateToken(authentication);

        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "ABCD";

        // when
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // then
        assertFalse(isValid);
    }

    @Test
    void givenValidJwtToken_whenGetUserIdFromJWT_thenReturnCorrectUserId() {
        // given
        String expectedUserId = "uniqueSpotifyId789";
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn(expectedUserId);
        when(oAuth2User.getAttribute("display_name")).thenReturn("User ID Extractor");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        String token = jwtTokenProvider.generateToken(authentication);

        // when
        String actualUserId = jwtTokenProvider.getUserIdFromJWT(token);

        // then
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    void givenValidJwtToken_whenGetAllClaimsFromJWT_thenReturnAllClaimsCorrectly() {
        // given
        String expectedUserId = "claimsUser123";
        String expectedUserName = "Claims Test User";
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn(expectedUserId);
        when(oAuth2User.getAttribute("display_name")).thenReturn(expectedUserName);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        String token = jwtTokenProvider.generateToken(authentication);

        // when
        Claims claims = jwtTokenProvider.getAllClaimsFromJWT(token);

        // then
        assertNotNull(claims);
        assertEquals(expectedUserId, claims.get("userId"));
        assertEquals(expectedUserName, claims.get("userName"));
        assertEquals(expectedUserName, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void givenUnsupportedJwtToken_whenValidateToken_thenReturnFalse() {
        // given
        String unsupportedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.unsupported.token.format";

        // when
        boolean isValid = jwtTokenProvider.validateToken(unsupportedToken);

        // then
        assertFalse(isValid);
    }

    @Test
    void givenMalformedJwtToken_whenValidateToken_thenReturnFalse() {
        // given
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.SflKx_invalid";

        // when
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // then
        assertFalse(isValid);
    }

    @Test
    void givenEmptyJwtToken_whenValidateToken_thenThrowIllegalArgumentExceptionAndReturnFalse() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // then
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.getAllClaimsFromJWT(emptyToken));
        assertFalse(isValid);
    }

    @Test
    void givenWrongSignatureJwtToken_whenGetAllClaimsFromJWT_thenThrowSignatureException() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("signatureUser");
        when(oAuth2User.getAttribute("display_name")).thenReturn("Signature Test User");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        String validToken = jwtTokenProvider.generateToken(authentication);

        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XYZAB";

        // when / then
        assertThrows(SignatureException.class, () -> jwtTokenProvider.getAllClaimsFromJWT(tamperedToken));
    }

    @Test
    void givenExpiredJwtToken_whenGetAllClaimsFromJWT_thenThrowExpiredJwtException() {
        // given
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 1);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("expiredUserClaims");
        when(oAuth2User.getAttribute("display_name")).thenReturn("Expired Claims Test User");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        String expiredToken = jwtTokenProvider.generateToken(authentication);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when / then
        assertThrows(ExpiredJwtException.class, () -> jwtTokenProvider.getAllClaimsFromJWT(expiredToken));
    }

    @Test
    void givenMalformedJwtToken_whenGetAllClaimsFromJWT_thenThrowMalformedJwtException() {
        // given
        String malformedToken = "header.payload.signature";

        // when / then
        assertThrows(MalformedJwtException.class, () -> jwtTokenProvider.getAllClaimsFromJWT(malformedToken));
    }

    @Test
    void givenNullJwtToken_whenGetAllClaimsFromJWT_then() {
        // given
        String nullToken = null;

        // when / then
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.getAllClaimsFromJWT(nullToken));
    }
}
