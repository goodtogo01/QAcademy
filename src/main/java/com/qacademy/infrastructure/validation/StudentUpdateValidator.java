package com.qacademy.infrastructure.validation;

import com.qacademy.core.dto.StudentUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class StudentUpdateValidator {

    private static final String DOB_PATTERN = "^\\d{2}/\\d{2}/\\d{4}$";
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    public ValidationResult validate(StudentUpdateDto dto) {
        ValidationResult result = new ValidationResult();

        result.addErrorIfTrue(isBlank(dto.firstName()), "firstName is required.");
        result.addErrorIfTrue(!isBlank(dto.firstName()) && dto.firstName().length() > 50,
                "firstName must be at most 50 characters.");

        result.addErrorIfTrue(isBlank(dto.lastName()), "lastName is required.");
        result.addErrorIfTrue(!isBlank(dto.lastName()) && dto.lastName().length() > 50,
                "lastName must be at most 50 characters.");

        result.addErrorIfTrue(isBlank(dto.email()), "email is required.");
        result.addErrorIfTrue(!isBlank(dto.email()) && !dto.email().matches(EMAIL_PATTERN),
                "email must be a valid email address.");

        result.addErrorIfTrue(isBlank(dto.dateOfBirth()), "dateOfBirth is required.");
        result.addErrorIfTrue(!isBlank(dto.dateOfBirth()) && !dto.dateOfBirth().matches(DOB_PATTERN),
                "dateOfBirth must be in DD/MM/YYYY format.");

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
