package com.luis.spotify.config;

import com.luis.spotify.service.impl.JwtTokenProviderServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private JwtTokenProviderServiceImpl jwtTokenProviderService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenNoAuthorizationHeader_whenDoFilterInternal_thenSecurityContextIsNotSet() throws ServletException, IOException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProviderService);
    }

    @Test
    void givenAuthorizationHeaderWithoutBearer_whenDoFilterInternal_thenSecurityContextIsNotSet() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Basic someToken");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProviderService);
    }

    @Test
    void givenInvalidJwtToken_whenDoFilterInternal_thenSecurityContextIsNotSet() throws ServletException, IOException {
        // given
        String invalidToken = "some.invalid.jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProviderService.validateToken(invalidToken)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProviderService).validateToken(invalidToken);
        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(jwtTokenProviderService);
    }

    @Test
    void givenValidJwtToken_whenDoFilterInternal_thenSecurityContextIsSet() throws ServletException, IOException {
        // given
        String validToken = "some.valid.jwt";
        String userId = "testUserId123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProviderService.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProviderService.getUserIdFromJWT(validToken)).thenReturn(userId);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, SecurityContextHolder.getContext().getAuthentication());
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(userId, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertEquals(Collections.emptyList(), authentication.getAuthorities());

        verify(jwtTokenProviderService).validateToken(validToken);
        verify(jwtTokenProviderService).getUserIdFromJWT(validToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void givenValidJwtTokenWithEmptyUserId_whenDoFilterInternal_thenSecurityContextIsSetWithEmptyUserId() throws ServletException, IOException {
        // given
        String validToken = "some.valid.jwt";
        String emptyUserId = "";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProviderService.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProviderService.getUserIdFromJWT(validToken)).thenReturn(emptyUserId);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(emptyUserId, authentication.getPrincipal());
        verify(jwtTokenProviderService).validateToken(validToken);
        verify(jwtTokenProviderService).getUserIdFromJWT(validToken);
        verify(filterChain).doFilter(request, response);
    }
}
