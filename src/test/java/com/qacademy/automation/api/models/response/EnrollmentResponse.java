package com.qacademy.automation.api.models.response;

/**
 * Response payload for an enrollment record.
 */
public record EnrollmentResponse (
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseName,
        String enrollmentDate,
        String grade) {

}
