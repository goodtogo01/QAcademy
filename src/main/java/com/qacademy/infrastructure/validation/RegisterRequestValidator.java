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

        boolean validRole = dto.role() != null
                && (dto.role().equalsIgnoreCase("Admin")
                    || dto.role().equalsIgnoreCase("Staff")
                    || dto.role().equalsIgnoreCase("Student"));
        result.addErrorIfTrue(!validRole, "role must be Admin, Staff, or Student.");

        return result;
    }
}
