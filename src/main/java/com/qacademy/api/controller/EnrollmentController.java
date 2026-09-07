package com.qacademy.api.controller;

import com.qacademy.core.dto.EnrollmentCreateDto;
import com.qacademy.core.dto.EnrollmentCreateResult;
import com.qacademy.core.dto.EnrollmentDto;
import com.qacademy.core.dto.EnrollmentUpdateDto;
import com.qacademy.core.service.EnrollmentService;
import com.qacademy.infrastructure.validation.EnrollmentCreateValidator;
import com.qacademy.infrastructure.validation.EnrollmentUpdateValidator;
import com.qacademy.infrastructure.validation.ValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentController {

    private final EnrollmentService service;
    private final EnrollmentCreateValidator createValidator;
    private final EnrollmentUpdateValidator updateValidator;

    public EnrollmentController(
            EnrollmentService service,
            EnrollmentCreateValidator createValidator,
            EnrollmentUpdateValidator updateValidator) {
        this.service = service;
        this.createValidator = createValidator;
        this.updateValidator = updateValidator;
    }

    // GETs are open for demo purposes - see SecurityConfig's permitAll rule for GET /api/enrollment/**
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<EnrollmentDto> enrollment = service.getById(id);
        if (enrollment.isPresent()) {
            return ResponseEntity.ok(enrollment.get());
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/enrollment/student/5 - all enrollments (and grades) for one student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    // POST /api/enrollment  { "studentId": 1, "courseId": 2 }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> create(@RequestBody EnrollmentCreateDto dto) {
        ValidationResult validation = createValidator.validate(dto);
        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(validation.getErrors());
        }

        EnrollmentCreateResult result = service.create(dto);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of("message", result.error()));
        }
        return ResponseEntity.status(201).body(result.enrollment());
    }

    // PUT /api/enrollment/3  { "grade": "A" }  - assign/update a grade
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateGrade(@PathVariable Long id, @RequestBody EnrollmentUpdateDto dto) {
        ValidationResult validation = updateValidator.validate(dto);
        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(validation.getErrors());
        }

        Optional<EnrollmentDto> updated = service.updateGrade(id, dto);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
