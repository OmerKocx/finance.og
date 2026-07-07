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


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CustomerFeignClient customerFeignClient;
    private final Mapper mapper;

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    private final KafkaProducerService kafkaProducerService;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Error: Email is already taken!");
        }

        String role;

        if (request.getRole() != null) {
            role = request.getRole().toUpperCase();
        } else {
            role = "USER";
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        CustomerDto customerDto = mapper.mapToCustomerDto(request);
        customerFeignClient.createCustomer(customerDto);

        String token = jwtService.generateToken(user);
        kafkaProducerService.sendRegisterEvent(new UserRegisterEvent(request.getFullName(), request.getEmail()));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(request.getFullName())
                .userId(user.getId())
                .build());
    }

    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new PasswordMismatchException("E-posta veya şifre hatalı!");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtService.generateToken(userDetails);

        String name = "";
        try {
            CustomerDto customer = customerFeignClient.getCustomerByEmail(userDetails.getUsername());
            if (customer != null) {
                name = customer.name();
            }
        } catch (Exception e) {
        }
        kafkaProducerService.sendLoginEvent(new UserLoginEvent(userDetails.getUsername()));

        User user = (User) userDetails;
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(userDetails.getUsername())
                .name(name)
                .userId(user.getId())
                .build());
    }

    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        String email = jwtService.extractEmail(token);

        UserDetails userDetails = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new InvalidTokenException("Token is invalid or expired!");
        }

        User user = (User) userDetails;

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        return ResponseEntity.ok(userDto);
    }
}