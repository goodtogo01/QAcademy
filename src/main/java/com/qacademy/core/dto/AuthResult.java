package com.qacademy.core.dto;

// Equivalent of the C# tuple (bool Success, string? Error) return type used by
// qEducation/qCampus's IAuthService.RegisterAsync.
public record AuthResult(boolean success, String error) {
}
