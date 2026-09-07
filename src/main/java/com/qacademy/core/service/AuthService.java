package com.qacademy.core.service;

import com.qacademy.core.dto.AuthResult;
import com.qacademy.core.dto.LoginRequestDto;
import com.qacademy.core.dto.LoginResponseDto;
import com.qacademy.core.dto.RegisterRequestDto;

import java.util.Optional;

public interface AuthService {
    Optional<LoginResponseDto> login(LoginRequestDto request);

    AuthResult register(RegisterRequestDto request);
}
