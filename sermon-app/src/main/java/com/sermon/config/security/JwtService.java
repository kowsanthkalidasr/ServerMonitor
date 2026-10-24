package com.sermon.config.security;


import com.sermon.logging.Loggers;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;


@Component
public class JwtService {


    private static final Logger securityLogger = LoggerFactory.getLogger(Loggers.SECURITY_LOG.name());

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;


    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return generateToken(userPrincipal);
    }

    public String generateToken(UserDetails userPrincipal) {
        List<String> roles = userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("roles",roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }


    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            securityLogger.error("JWT Token is expired - {}" , e.getMessage(),e);
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            securityLogger.error("Suspicious Behaviour | Invalid JWT Token - {} ", e.getMessage(),e);
        } catch (IllegalArgumentException e) {
            securityLogger.error("Request error | Invalid JWT Token - {} ", e.getMessage(),e);
        } catch (Exception e) {
            securityLogger.error("Error in validating the JWT Token - {}", e.getMessage(),e);
        }

        return null;
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            securityLogger.error("Failed to check token expiration - {} " , e.getMessage(),e);
            return true;
        }
    }


    public Date extractIssuedTime(String token) {
        return extractClaim(token, claim -> claim.getIssuedAt());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, claim -> claim.getExpiration());
    }



    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    public <T> T extractClaim (String token, Function<Claims,T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
