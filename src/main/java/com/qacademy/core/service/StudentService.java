package com.qacademy.core.service;

import com.qacademy.core.dto.StudentCreateDto;
import com.qacademy.core.dto.StudentDto;
import com.qacademy.core.dto.StudentUpdateDto;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    List<StudentDto> getAll();

    Optional<StudentDto> getById(Long id);

    StudentDto create(StudentCreateDto dto);

    boolean update(Long id, StudentUpdateDto dto);

    boolean delete(Long id);
}
