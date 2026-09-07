package com.qacademy.api.controller;

import com.qacademy.core.dto.CourseCreateDto;
import com.qacademy.core.dto.CourseDto;
import com.qacademy.core.service.CourseService;
import com.qacademy.infrastructure.validation.CourseCreateValidator;
import com.qacademy.infrastructure.validation.ValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService service;
    private final CourseCreateValidator createValidator;

    public CourseController(CourseService service, CourseCreateValidator createValidator) {
        this.service = service;
        this.createValidator = createValidator;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<CourseDto> course = service.getById(id);
        if (course.isPresent()) {
            return ResponseEntity.ok(course.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody CourseCreateDto dto) {
        ValidationResult validation = createValidator.validate(dto);
        if (!validation.isValid()) {
            return ResponseEntity.badRequest().body(validation.getErrors());
        }

        CourseDto created = service.create(dto);
        return ResponseEntity.status(201).body(created);
    }
}
