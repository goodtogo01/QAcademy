package com.qacademy.infrastructure.validation;

import com.qacademy.core.dto.RegisterRequestDto;
import org.springframework.stereotype.Component;

@Component
public class RegisterRequestValidator {

    public ValidationResult validate(RegisterRequestDto dto) {
        ValidationResult result = new ValidationResult();

        result.addErrorIfTrue(dto.username() == null || dto.username().isBlank(), "username is required.");
        result.addErrorIfTrue(dto.username() != null && dto.username().length() > 50,
                "username must be at most 50 characters.");

        result.addErrorIfTrue(dto.password() == null || dto.password().length() < 8,
                "password must be at least 8 characters.");

        // Admin is deliberately NOT a self-registerable role: /api/auth/register is a
        // permitAll endpoint (anyone, no token), so allowing "Admin" here let any
        // anonymous visitor create a full admin account - a privilege-escalation hole
        // (the register form's UI used to even list it as a dropdown option). The one
        // admin account this app ships with is created by DataSeeder at startup instead;
        // provisioning further admins is an operational action, not a public signup flow.
        boolean validRole = dto.role() != null
                && (dto.role().equalsIgnoreCase("Staff")
                    || dto.role().equalsIgnoreCase("Student"));
        result.addErrorIfTrue(!validRole, "role must be Staff or Student.");

        return result;
    }
}
