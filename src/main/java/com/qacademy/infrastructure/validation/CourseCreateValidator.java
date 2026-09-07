package com.qacademy.infrastructure.validation;

import com.qacademy.core.dto.CourseCreateDto;
import org.springframework.stereotype.Component;

@Component
public class CourseCreateValidator {

    public ValidationResult validate(CourseCreateDto dto) {
        ValidationResult result = new ValidationResult();

        result.addErrorIfTrue(dto.name() == null || dto.name().isBlank(), "name is required.");
        result.addErrorIfTrue(dto.name() != null && dto.name().length() > 100,
                "name must be at most 100 characters.");

        result.addErrorIfTrue(dto.credits() == null, "credits is required.");
        result.addErrorIfTrue(dto.credits() != null && (dto.credits() < 1 || dto.credits() > 6),
                "credits must be between 1 and 6.");

        return result;
    }
}
