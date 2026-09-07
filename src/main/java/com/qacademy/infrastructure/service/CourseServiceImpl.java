package com.qacademy.infrastructure.service;

import com.qacademy.core.dto.CourseCreateDto;
import com.qacademy.core.dto.CourseDto;
import com.qacademy.core.entity.Course;
import com.qacademy.core.service.CourseService;
import com.qacademy.infrastructure.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repository;

    public CourseServiceImpl(CourseRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CourseDto> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public Optional<CourseDto> getById(Long id) {
        return repository.findById(id).map(this::toDto);
    }

    @Override
    public CourseDto create(CourseCreateDto dto) {
        Course course = new Course();
        course.setName(dto.name());
        course.setCredits(dto.credits());

        Course saved = repository.save(course);
        return toDto(saved);
    }

    private CourseDto toDto(Course c) {
        return new CourseDto(c.getId(), c.getName(), c.getCredits());
    }
}
