package com.qacademy.infrastructure.repository;

import com.qacademy.core.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Spring Data resolves "StudentId" by traversing the student.id nested
    // property automatically, since Enrollment has no direct studentId field -
    // no @Query needed for this one.
    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);
}
