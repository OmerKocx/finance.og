package com.omerkoc.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JwtUtil, gelen JWT token'larının imzasını doğrulamakla görevli yardımcı sınıftır.
 */
@Component
public class JwtUtil {

    // Config Server'dan (gateway-service.yml) enjekte edilen JWT gizli anahtarı
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Gelen JWT token'ını doğrular.
     * Eğer token'ın imzası geçersizse, yapısı bozuksa veya süresi dolmuşsa (expired) 
     * parseSignedClaims metodu doğrudan bir hata (Exception) fırlatır.
     * 
     * @param token Doğrulanacak ham JWT String'i (Bearer kısmı olmadan)
     */
    public void validateToken(final String token) {
        Jwts.parser()
                .verifyWith(getSignInKey()) // İmzayı doğrulamak için gizli anahtarımızı tanımlıyoruz
                .build()
                .parseSignedClaims(token); // Token'ı çözümler ve imza kontrolünü yapar (hata olursa exception atar)
    }

    /**
     * String formatındaki gizli anahtarı, JJWT kütüphanesinin kullanabileceği
     * HMAC-SHA algoritmasına uygun SecretKey nesnesine dönüştürür.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

