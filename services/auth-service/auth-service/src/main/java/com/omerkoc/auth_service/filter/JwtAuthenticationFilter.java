package com.omerkoc.auth_service.filter;

import com.omerkoc.auth_service.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Her HTTP isteğinde bir kez çalışan ve JWT tabanlı kimlik doğrulamayı
 * gerçekleştiren filtre sınıfı.
 * OncePerRequestFilter sınıfından türetilmiştir.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Filtrenin çalışacağı metot.
     * Her HTTP isteğinde çalışır, JWT kontrolünü yapar ve kullanıcıyı sisteme giriş
     * yaptırır.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // HTTP isteğinin başlığından (Header) "Authorization" değerini alıyoruz.
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        // "Authorization" başlığı boşsa veya "Bearer " ile başlamıyorsa istek
        // filtrelenmeden bir sonraki filtreye aktarılır.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " kısmını keserek token'ın kendisini (JWT) elde ediyoruz.
        jwt = authHeader.substring(7);
        try {
            // JWT token'ından kullanıcının e-posta adresini ayrıştırıyoruz.
            email = jwtService.extractEmail(jwt); // jwt servisten e maili aldı

            // E-posta adresi null değilse ve kullanıcının oturumu Spring Security
            // Context'te henüz kurulmamışsa (Authentication null ise):
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {// email null değilse
                                                                                                  // ama context null
                                                                                                  // ise login
                                                                                                  // yapıyordur
                // Veritabanından kullanıcı bilgilerini güncel olarak çekiyoruz.
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                // Token'ın geçerliliğini ve süresinin dolup dolmadığını doğruluyoruz.
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Kullanıcı bilgilerini ve yetkilerini içeren bir Authentication token nesnesi
                    // oluşturuyoruz.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    // İsteğe ait detayları token nesnesine set ediyoruz.
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    // Spring Security SecurityContextHolder'a bu token'ı kaydederek kullanıcının
                    // sisteme giriş yaptığını onaylıyoruz.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token geçersiz veya süresi dolmuşsa kimlik doğrulama işlemi yapılmaz ve istek
            // bu şekilde devam eder.
        }

        // İsteği filtre zincirindeki bir sonraki filtreye iletiyoruz.
        filterChain.doFilter(request, response);
    }
}
