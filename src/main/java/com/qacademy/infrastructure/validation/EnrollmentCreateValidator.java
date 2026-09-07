package com.qacademy.infrastructure.validation;

import com.qacademy.core.dto.EnrollmentCreateDto;
import org.springframework.stereotype.Component;

// Only shape-checks the DTO (non-null ids). Whether those ids actually reference
// an existing Student/Course is a database question, not a DTO-validation one -
// that check lives in EnrollmentServiceImpl.create(), same separation of concerns
// as everywhere else in this project.
@Component
public class EnrollmentCreateValidator {

    public ValidationResult validate(EnrollmentCreateDto dto) {
        ValidationResult result = new ValidationResult();

        result.addErrorIfTrue(dto.studentId() == null, "studentId is required.");
        result.addErrorIfTrue(dto.courseId() == null, "courseId is required.");

        return result;
    }
}
