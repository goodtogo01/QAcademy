package com.qacademy.api.controller;

import com.qacademy.core.dto.StudentCreateDto;
import com.qacademy.core.dto.StudentDto;
import com.qacademy.core.dto.StudentUpdateDto;
import com.qacademy.core.service.StudentService;
import com.qacademy.infrastructure.validation.StudentCreateValidator;
import com.qacademy.infrastructure.validation.StudentUpdateValidator;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService service;
    private final StudentCreateValidator createValidator;
    private final StudentUpdateValidator updateValidator;

    public StudentController(
            StudentService service,
            StudentCreateValidator createValidator,
            StudentUpdateValidator updateValidator) {
        this.service = service;
        this.createValidator = createValidator;
        this.updateValidator = updateValidator;
    }

    // GETs are open for demo purposes - see SecurityConfig's permitAll rule for GET /api/student/**
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<StudentDto> student = service.getById(id);
        if (student.isPresent()) {
            return ResponseEntity.ok(student.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> create(@RequestBody StudentCreateDto dto) {
        ValidationResult validation = createValidator.validate(dto);
        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(validation.getErrors());
        }

        StudentDto created = service.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StudentUpdateDto dto) {
        ValidationResult validation = updateValidator.validate(dto);
        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(validation.getErrors());
        }

        boolean updated = service.update(id, dto);
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
