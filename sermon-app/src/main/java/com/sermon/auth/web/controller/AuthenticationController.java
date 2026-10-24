package com.sermon.auth.web.controller;


import com.sermon.auth.service.AuthenticationService;
import com.sermon.auth.web.dto.AuthResponse;
import com.sermon.auth.web.dto.LoginRequest;
import com.sermon.logging.Loggers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")

public class AuthenticationController {

    private static final Logger securityLogger = LoggerFactory.getLogger(Loggers.SECURITY_LOG.name());

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest request) {
        System.out.println("Request in controller : " + request.toString());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
