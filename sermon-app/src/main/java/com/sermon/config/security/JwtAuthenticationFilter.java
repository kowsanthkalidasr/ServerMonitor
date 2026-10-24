package com.sermon.config.security;

import com.sermon.logging.Loggers;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger securityLogger = LoggerFactory.getLogger(Loggers.SECURITY_LOG.name());

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        securityLogger.info("Processing JWT authorization for URI {} " ,request.getRequestURI());

        String jwt = parseJwt(request);

        if(jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtService.validateAndGetClaims(jwt);

            if (claims != null) {
                String extractedUsername = claims.getSubject();

                if(extractedUsername != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(extractedUsername);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null,userDetails.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    securityLogger.info("Successfully authenticated user {} for URI {} ", extractedUsername,request.getRequestURI());
                } else {
                    securityLogger.warn("JWT Signature verified successfully but Subject/Username claim is null for URI {} ", request.getRequestURI());
                }
            } else {
                securityLogger.warn("JWT token validation failed for URI: {}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);

    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            return headerAuth.substring(BEARER_PREFIX.length());
        }
        return null;
    }



}
