package com.omerkoc.auth_service.controller;

import com.omerkoc.auth_service.dto.*;
import com.omerkoc.auth_service.model.User;
import com.omerkoc.auth_service.repository.UserRepository;
import com.omerkoc.auth_service.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Kimlik doğrulama (Authentication) işlemlerini yöneten REST Controller sınıfı.
 * Kullanıcı kaydı (register), kullanıcı girişi (login) ve JWT token doğrulama
 * (validate)
 * endpoint'lerini barındırır.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    // Spring Security'nin kimlik doğrulama mekanizmasını tetikleyen yönetici sınıf
    private final AuthenticationManager authenticationManager;

    // Kullanıcı detaylarını e-posta ile veritabanından getiren servis
    private final UserDetailsService userDetailsService;

    // Veritabanı işlemleri için kullanıcı reposu
    private final UserRepository userRepository;

    // Şifreleri BCrypt ile güvenli şekilde hash'lemek için kullanılan encoder
    private final PasswordEncoder passwordEncoder;

    // JWT üretme, ayrıştırma ve doğrulama işlemlerini yürüten servis
    private final JwtService jwtService;

    /**
     * Yeni bir kullanıcının sisteme kayıt olmasını sağlayan endpoint.
     * 
     * @param request Kayıt bilgileri (email, şifre, rol)
     * @return Başarılıysa üretilen JWT token'ı ve kullanıcı bilgilerini döner.
     */
    @PostMapping("/register")
    // request passowrd email ve rolleri içerir
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // request email password içerir
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Email is already taken!");
        }

        String role;

        // 2. Kullanıcı rolünü belirliyoruz (Belirtilmemişse varsayılan olarak "USER"
        // atıyoruz)
        if (request.getRole() != null) {
            role = request.getRole().toUpperCase(); // Gelen rolü al, BÜYÜK HARF yap.
        } else {
            role = "USER"; // Rol gelmediyse direkt USER
        }

        // 3. Şifreyi güvenli bir şekilde hash'leyerek yeni kullanıcıyı oluşturuyoruz
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        // 4. Kullanıcıyı veritabanına kaydediyoruz
        userRepository.save(user);

        // 5. Kayıt olan kullanıcı için hemen bir JWT token üretiyoruz
        String token = jwtService.generateToken(user);

        // 6. Başarılı HTTP 200 yanıtı ile token ve email bilgisini dönüyoruz
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .build());
    }

    /**
     * Kayıtlı kullanıcının sisteme giriş yapmasını sağlayan endpoint.
     * 
     * @param request Giriş bilgileri (email, şifre)
     * @return Başarılıysa üretilen JWT token'ı ve kullanıcı bilgilerini döner.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // 1. Spring Security AuthenticationManager ile email ve şifreyi doğruluyoruz.
        // E-posta bulunamazsa veya şifre yanlışsa bu metot otomatik olarak exception
        // fırlatır ve giriş engellenir.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // 2. Doğrulama başarılıysa veritabanından kullanıcı bilgilerini (UserDetails)
        // çekiyoruz
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // 3. Kullanıcıya özel JWT token üretiyoruz
        String token = jwtService.generateToken(userDetails);

        // 4. Token ve kullanıcının e-posta adresini içeren yanıtı dönüyoruz
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(userDetails.getUsername())
                .build());
    }

    /**
     * Dışarıdan (örneğin API Gateway) gelen JWT token'ın geçerliliğini doğrulayan
     * endpoint.
     * 
     * @param token Doğrulanacak JWT token string'i
     * @return Token geçerliyse kullanıcının ID, Email ve Rol bilgilerini döner
     *         (Yetkilendirme için).
     */
    // loginrequest email ve passowrd ı tutar sadece
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        try {
            // 1. Token içerisinden kullanıcının e-posta adresini (subject) çıkarıyoruz
            String email = jwtService.extractEmail(token);

            // 2. E-posta adresiyle veritabanından kullanıcıyı sorguluyoruz
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 3. Token'ın geçerliliğini (süre dolumu ve e-posta eşleşmesi) kontrol ediyoruz
            if (jwtService.isTokenValid(token, userDetails)) {
                User user = (User) userDetails;

                // 4. Token geçerliyse, diğer mikroservislerin kullanabilmesi için UserDto
                // nesnesi oluşturup dönüyoruz
                UserDto userDto = UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build();
                return ResponseEntity.ok(userDto);
            }

            // 5. Token geçerli değilse veya uyuşmazlık varsa 401 Unauthorized dönüyoruz
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired");
        } catch (Exception e) {
            // 6. Herhangi bir beklenmedik hata durumunda (yanlış token formatı vb.) yine
            // 401 Unauthorized dönüyoruz
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token is invalid or expired: " + e.getMessage());
        }
    }
}
