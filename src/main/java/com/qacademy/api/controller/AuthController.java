package com.qacademy.api.controller;

import com.qacademy.core.dto.AuthResult;
import com.qacademy.core.dto.LoginRequestDto;
import com.qacademy.core.dto.LoginResponseDto;
import com.qacademy.core.dto.RegisterRequestDto;
import com.qacademy.core.service.AuthService;
import com.qacademy.infrastructure.validation.RegisterRequestValidator;
import com.qacademy.infrastructure.validation.ValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegisterRequestValidator registerValidator;

    public AuthController(AuthService authService, RegisterRequestValidator registerValidator) {
        this.authService = authService;
        this.registerValidator = registerValidator;
    }

    // POST /api/auth/login  { "username": "admin", "password": "Admin123!" }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Optional<LoginResponseDto> result = authService.login(request);
        if (result.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        }
        return ResponseEntity.ok(result.get());
    }

    // POST /api/auth/register  { "username": "newuser", "password": "Pass1234", "role": "Staff" }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        ValidationResult validation = registerValidator.validate(request);
        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(validation.getErrors());
        }

        AuthResult result = authService.register(request);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of("message", result.error()));
        }
        return ResponseEntity.ok(Map.of("message", "User created successfully"));
    }
}
