package com.sermon.auth.web.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record AuthResponse(
        String token,
        String type,
        Date issuedAt,
        Date expiresIn
) {
}
