package com.qacademy.core.dto;

import java.time.Instant;

public record LoginResponseDto(String token, Instant expiresAtUtc) {
}
