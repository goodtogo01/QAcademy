package com.qacademy.core.service;

import com.qacademy.core.dto.EnrollmentCreateDto;
import com.qacademy.core.dto.EnrollmentCreateResult;
import com.qacademy.core.dto.EnrollmentDto;
import com.qacademy.core.dto.EnrollmentUpdateDto;

import java.util.List;
import java.util.Optional;

public interface EnrollmentService {
    List<EnrollmentDto> getAll();

    Optional<EnrollmentDto> getById(Long id);

    List<EnrollmentDto> getByStudentId(Long studentId);

    // Fails with a message (not just false) if studentId or courseId doesn't exist.
    EnrollmentCreateResult create(EnrollmentCreateDto dto);

    Optional<EnrollmentDto> updateGrade(Long id, EnrollmentUpdateDto dto);

    boolean delete(Long id);
}
