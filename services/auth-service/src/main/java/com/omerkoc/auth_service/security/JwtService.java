package com.omerkoc.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT (JSON Web Token) işlemlerini yöneten servis sınıfı.
 * Token üretme, doğrulama, token içerisinden e-posta/claim çıkarma ve
 * imzalama anahtarı oluşturma gibi işlemleri gerçekleştirir.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * JWT token'ından kullanıcının e-posta adresini (subject) ayrıştırır.
     */
    // email i alır
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Token'daki Claims (veriler) içinden belirli bir bilgiyi ayrıştırmak için
     * kullanılan genel (generic) metot.
     * Parametre olarak gönderilen claimsResolver fonksiyonu ile istenen veriyi
     * (örn: son kullanma tarihi) döner.
     */
    // tokendan herhangi bir bilgiyi almak için kullanılır
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Sadece kullanıcı bilgilerini (UserDetails) içeren temel bir JWT token üretir.
     */
    // sadece userdetails ile token üretir
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Kullanıcı bilgiyeiyle beraber eklemek istediğimiz ekstra bilgileri de
     * (extraClaims) barındıran bir JWT token üretir.
     */
    // extraClaims ile token üretir hem userdetails hemde ekstra ne ekliyorsan mapde
    // o yüzden var zaten
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Yapılandırma dosyasındaki JWT token geçerlilik süresini (milisaniye
     * cinsinden) döner.
     */
    // sadece exp i görmek için
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Verilen parametrelere göre JWT token'ı inşa eden yardımcı metot.
     * Token'ın içeriğini (claims, subject, issuedAt, expiration) belirler ve gizli
     * anahtar ile imzalar.
     */
    // en alttaki buildtoken sadece tokenı birleştirir o kadar
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * JWT token'ının geçerli olup olmadığını doğrular.
     * Token içindeki e-posta ile kullanıcı bilgilerindeki e-postanın eşleşmesini ve
     * süresinin dolup dolmadığını kontrol eder.
     */
    // tokendan e maili alır ve tokenın geçerli olup olmadığını kontrol eder
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Bir kullanıcı nesnesi olmadan, sadece token'ın süresinin dolup dolmadığını
     * kontrol etmek için kullanılır.
     */
    public boolean isTokenValidWithoutUser(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Token'ın geçerlilik süresinin dolup dolmadığını kontrol eden yardımcı metot.
     */
    // tokendan exp i alır ve süresi dolmuş mu diye bakar
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Token'ın son kullanma tarihini (Expiration claim) çözümler.
     */
    // tokendan exp i alır
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Token içindeki tüm Claims (veri) paketini çözer.
     * İmzayı gizli anahtar ile doğrulayarak payload (veri) kısmını elde eder.
     */
    // tokendan her şeyi alır
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT imzalamada kullanılan HMAC-SHA algoritması için gizli anahtar (SecretKey)
     * oluşturur.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
