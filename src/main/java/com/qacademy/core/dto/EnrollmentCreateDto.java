package com.qacademy.core.dto;

// Grade is intentionally not part of creation - a student enrolls first, a grade
// gets assigned later via EnrollmentUpdateDto (PUT /api/enrollment/{id}).
public record EnrollmentCreateDto(Long studentId, Long courseId) {
}
