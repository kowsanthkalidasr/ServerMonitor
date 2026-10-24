package com.sermon.auth.service;


import com.sermon.auth.web.dto.AuthResponse;
import com.sermon.auth.web.dto.LoginRequest;
import com.sermon.config.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    private final String TOKEN_TYPE_BEARER = "Bearer";


    public AuthResponse authenticate (LoginRequest request) {

        authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
                );

        UserDetails user =  customUserDetailsService.loadUserByUsername(request.email());

        String jwtToken = jwtService.generateToken(user);


        return  AuthResponse.builder()
                .token(jwtToken)
                .type(TOKEN_TYPE_BEARER)
                .expiresIn(jwtService.extractExpiration(jwtToken))
                .issuedAt(jwtService.extractIssuedTime(jwtToken))
                .build();

    }



}
