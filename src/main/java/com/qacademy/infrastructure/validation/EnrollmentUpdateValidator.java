package com.qacademy.infrastructure.validation;

import com.qacademy.core.dto.EnrollmentUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentUpdateValidator {

    // Letter grade, optional +/- (A, B+, C-, F, etc.)
    private static final String GRADE_PATTERN = "^[A-Fa-f][+-]?$";

    public ValidationResult validate(EnrollmentUpdateDto dto) {
        ValidationResult result = new ValidationResult();

        boolean blank = dto.grade() == null || dto.grade().isBlank();
        result.addErrorIfTrue(blank, "grade is required.");
        result.addErrorIfTrue(!blank && !dto.grade().matches(GRADE_PATTERN),
                "grade must be a letter grade like A, B+, or C- (max one letter plus an optional +/-).");

        return result;
    }
}
