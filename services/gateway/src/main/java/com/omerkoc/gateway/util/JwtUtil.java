package com.omerkoc.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Validates the JWT token.
     * Throws an exception if the signature or structure is invalid, or if the token
     * is expired.
     */
    public void validateToken(final String token) {
        Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
