package com.qacademy.infrastructure.service;

import com.qacademy.core.dto.StudentCreateDto;
import com.qacademy.core.dto.StudentDto;
import com.qacademy.core.dto.StudentUpdateDto;
import com.qacademy.core.entity.Student;
import com.qacademy.core.service.StudentService;
import com.qacademy.infrastructure.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;

    // Constructor injection - the service depends on the abstraction
    // (StudentRepository, itself an interface), which is what makes unit testing
    // possible: tests inject a Mockito mock instead of a real database.
    public StudentServiceImpl(StudentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StudentDto> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public Optional<StudentDto> getById(Long id) {
        return repository.findById(id).map(this::toDto);
    }

    @Override
    public StudentDto create(StudentCreateDto dto) {
        Student student = new Student();
        student.setFirstName(dto.firstName());
        student.setLastName(dto.lastName());
        student.setEmail(dto.email());
        student.setDateOfBirth(dto.dateOfBirth());

        Student saved = repository.save(student);
        return toDto(saved);
    }

    @Override
    public boolean update(Long id, StudentUpdateDto dto) {
        Optional<Student> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }

        Student student = existing.get();
        student.setFirstName(dto.firstName());
        student.setLastName(dto.lastName());
        student.setEmail(dto.email());
        student.setDateOfBirth(dto.dateOfBirth());

        repository.save(student);
        return true;
    }

    @Override
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private StudentDto toDto(Student s) {
        return new StudentDto(s.getId(), s.getFirstName(), s.getLastName(), s.getEmail(), s.getDateOfBirth());
    }
}
