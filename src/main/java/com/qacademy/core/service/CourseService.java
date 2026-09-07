package com.qacademy.core.service;

import com.qacademy.core.dto.CourseCreateDto;
import com.qacademy.core.dto.CourseDto;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<CourseDto> getAll();

    Optional<CourseDto> getById(Long id);

    CourseDto create(CourseCreateDto dto);
}
