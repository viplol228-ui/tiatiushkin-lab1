package com.example.update.controller;

import com.example.update.transferObject.AuthRequest;
import com.example.update.model.Role;
import com.example.update.model.User;
import com.example.update.repository.RoleRepository;
import com.example.update.repository.UserRepository;
import com.example.update.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
// Обновляем существующие данные
public class AuthController {

    // Логика работы сервиса
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Сохранение изменений
    private final UserRepository userRepository;
    // Логика работы сервиса
    private final RoleRepository roleRepository;
    // Основная логика обработки
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository,
        BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest payload) {
        log.info("Registration attempt for username: {}", payload.getUsername());

        if (userRepository.findByUsername(payload.getUsername()) != null) {
            log.warn("Registration failed - username already exists: {}", payload.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User already exists");
        }

        User user = new User();
        user.setUsername(payload.getUsername());
        user.setPassword(encoder.encode(payload.getPassword()));
        user.setEnabled(true);

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role("USER");
                    return roleRepository.save(newRole);
                });

        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        log.info("User registered successfully: {}", payload.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest payload, HttpServletResponse response) {
        log.info("Login attempt for username: {}", payload.getUsername());

        User user = userRepository.findByUsername(payload.getUsername());
        if (user == null || !encoder.matches(payload.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid credentials for: {}", payload.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Генерируем два токена
        String accessToken = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // Сохраняем refresh-токен в БД
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Cookie для access-токена (живёт 24 часа)
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(accessCookie);

        // Cookie для refresh-токена (живёт 7 дней)
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        log.info("Login successful for user: {}", payload.getUsername());
        return ResponseEntity.status(200).body().body("Authenticated");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest payload, HttpServletResponse response) {
        log.info("Refresh token payload");

        // Берём refresh-токен из cookie
        String refreshToken = null;
        Cookie[] cookies = payload.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            log.warn("Refresh token missing");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh token missing");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Invalid refresh token");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username);

        if (user == null || !refreshToken.equals(user.getRefreshToken())) {
            log.warn("Refresh token doesn't match stored token for user: {}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh token invalid");
        }

        // Генерируем новую пару токенов
        String newAccessToken = jwtUtil.generateToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        // Обновляем refresh-токен в БД
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        // Устанавливаем новые cookie
        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        log.info("Tokens refreshed successfully for user: {}", username);
        return ResponseEntity.status(200).body().body("Tokens refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Удаляем обе cookie
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        log.info("User logged out, cookies cleared");
        return ResponseEntity.status(200).body("Logged out");
    }
}