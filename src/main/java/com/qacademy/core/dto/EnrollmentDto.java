package com.qacademy.core.dto;

import java.time.LocalDateTime;

// studentName/courseName are denormalized display fields (not raw entity
// navigation) - the DTO stays flat rather than nesting full StudentDto/CourseDto
// objects, same "DTOs are never the raw entity" philosophy as the rest of the DTOs.
public record EnrollmentDto(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseName,
        LocalDateTime enrollmentDate,
        String grade) {
}
