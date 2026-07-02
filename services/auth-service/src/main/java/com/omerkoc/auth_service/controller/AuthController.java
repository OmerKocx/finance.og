package com.omerkoc.auth_service.controller;

import com.omerkoc.auth_service.FeignClient.CustomerDto;
import com.omerkoc.auth_service.FeignClient.CustomerFeignClient;
import com.omerkoc.auth_service.dto.*;
import com.omerkoc.auth_service.exception.*;
import com.omerkoc.auth_service.mapper.Mapper;
import com.omerkoc.auth_service.model.User;
import com.omerkoc.auth_service.repository.UserRepository;
import com.omerkoc.auth_service.security.JwtService;
import com.omerkoc.auth_service.service.KafkaProducerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final CustomerFeignClient customerFeignClient;
    private final Mapper mapper;

    // Spring Security'nin kimlik doğrulama mekanizmasını tetikleyen yönetici sınıf
    private final AuthenticationManager authenticationManager;

    // Kullanıcı detaylarını e-posta ile veritabanından getiren servis
    private final UserDetailsService userDetailsService;

    // Veritabanı işlemleri için kullanıcı reposu
    private final UserRepository userRepository;

    private final KafkaProducerService kafkaProducerService;

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
            throw new EmailAlreadyExistsException("Error: Email is already taken!");
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

        // 5. Kullanıcı için müşteri oluşturuyoruz
        CustomerDto customerDto = mapper.mapToCustomerDto(request);
        customerFeignClient.createCustomer(customerDto);

        // 6. Kayıt olan kullanıcı için hemen bir JWT token üretiyoruz
        String token = jwtService.generateToken(user);
        // Kafka'ya Kayıt Event'i gönder
        kafkaProducerService.sendRegisterEvent(new UserRegisterEvent(request.getFullName(), request.getEmail()));

        // 7. Başarılı HTTP 200 yanıtı ile token, email ve ad bilgisini dönüyoruz
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(request.getFullName())
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new PasswordMismatchException("E-posta veya şifre hatalı!");
        }

        // 2. Doğrulama başarılıysa veritabanından kullanıcı bilgilerini (UserDetails)
        // çekiyoruz
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // 3. Kullanıcıya özel JWT token üretiyoruz
        String token = jwtService.generateToken(userDetails);

        // 4. Müşteri servisinden kullanıcının adını çekiyoruz
        String name = "";
        try {
            CustomerDto customer = customerFeignClient.getCustomerByEmail(userDetails.getUsername());
            if (customer != null) {
                name = customer.name();
            }
        } catch (Exception e) {
            // Hata durumunda isim boş bırakılır
        }
        // Kafka'ya Giriş Event'i gönder
        kafkaProducerService.sendLoginEvent(new UserLoginEvent(userDetails.getUsername()));

        // 5. Token, kullanıcının e-posta adresini ve adını içeren yanıtı dönüyoruz
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(userDetails.getUsername())
                .name(name)
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
        // 1. Token içerisinden kullanıcının e-posta adresini (subject) çıkarıyoruz
        String email = jwtService.extractEmail(token);

        // 2. E-posta adresiyle veritabanından kullanıcıyı sorguluyoruz
        UserDetails userDetails = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        // 3. Token'ın geçerliliğini (süre dolumu ve e-posta eşleşmesi) kontrol ediyoruz
        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new InvalidTokenException("Token is invalid or expired!");
        }

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
}
