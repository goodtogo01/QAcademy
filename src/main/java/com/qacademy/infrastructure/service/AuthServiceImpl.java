package com.qacademy.infrastructure.service;

import com.qacademy.core.dto.AuthResult;
import com.qacademy.core.dto.LoginRequestDto;
import com.qacademy.core.dto.LoginResponseDto;
import com.qacademy.core.dto.RegisterRequestDto;
import com.qacademy.core.entity.User;
import com.qacademy.core.entity.UserRole;
import com.qacademy.core.service.AuthService;
import com.qacademy.infrastructure.repository.UserRepository;
import com.qacademy.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Optional<LoginResponseDto> login(LoginRequestDto request) {
        Optional<User> userOpt = userRepository.findByUsername(request.username());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // BCryptPasswordEncoder.matches() is a constant-time comparison under the
        // hood - never compare hashes with equals()/==.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return Optional.empty();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return Optional.of(new LoginResponseDto(token, jwtUtil.getExpiration(token).toInstant()));
    }

    @Override
    public AuthResult register(RegisterRequestDto request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return new AuthResult(false, "Username already exists");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return new AuthResult(false, "Role must be Staff or Student");
        }
        // Defense-in-depth: RegisterRequestValidator already rejects "Admin" before this
        // point is reached, but this endpoint is unauthenticated (permitAll), so the
        // service layer refuses to create an Admin account here too rather than relying
        // on the validator alone to be the only thing standing between anonymous callers
        // and a privilege escalation.
        if (role == UserRole.ADMIN) {
            return new AuthResult(false, "Role must be Staff or Student");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        userRepository.save(user);

        return new AuthResult(true, null);
    }
}
