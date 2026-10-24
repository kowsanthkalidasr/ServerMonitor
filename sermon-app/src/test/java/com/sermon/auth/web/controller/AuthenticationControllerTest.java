package com.sermon.auth.web.controller;

import com.sermon.config.security.JwtAuthenticationFilter;
import com.sermon.config.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthenticationControllerTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter =
            new JwtAuthenticationFilter(jwtService, userDetailsService);

    @Test
    void jwtFilterContinuesFilterChainWhenNoAuthorizationHeaderIsPresent() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
