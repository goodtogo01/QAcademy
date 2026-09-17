package com.qacademy.infrastructure.validation;

import com.qacademy.core.dto.StudentUpdateDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@Component
public class StudentUpdateValidator {

    private static final String DOB_SHAPE_PATTERN = "^\\d{2}/\\d{2}/\\d{4}$";
    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
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

        validateDateOfBirth(dto.dateOfBirth(), result);

        return result;
    }

    private void validateDateOfBirth(String value, ValidationResult result) {
        result.addErrorIfTrue(isBlank(value), "dateOfBirth is required.");
        if (isBlank(value)) {
            return;
        }
        result.addErrorIfTrue(!value.matches(DOB_SHAPE_PATTERN), "dateOfBirth must be in DD/MM/YYYY format.");
        if (!value.matches(DOB_SHAPE_PATTERN)) {
            return;
        }
        try {
            LocalDate dateOfBirth = LocalDate.parse(value, DOB_FORMATTER);
            result.addErrorIfTrue(dateOfBirth.isAfter(LocalDate.now()), "dateOfBirth cannot be in the future.");
        } catch (DateTimeParseException e) {
            result.addErrorIfTrue(true,
                    "dateOfBirth '" + value + "' is not a real calendar date - check the day and month aren't swapped.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
