package com.qacademy.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;

// Small hand-rolled validation result - the Java equivalent of FluentValidation's
// ValidationResult in qCampus. Kept dependency-free on purpose (see README for why
// this project doesn't use Jakarta Bean Validation annotations): controllers call
// a Validator's validate() method explicitly, so the validation step stays visible
// rather than happening through annotation-driven auto-validation.
public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addErrorIfTrue(boolean condition, String message) {
        if (condition) {
            errors.add(message);
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }
}
