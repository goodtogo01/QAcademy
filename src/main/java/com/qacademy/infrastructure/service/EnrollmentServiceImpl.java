package com.qacademy.infrastructure.service;

import com.qacademy.core.dto.EnrollmentCreateDto;
import com.qacademy.core.dto.EnrollmentCreateResult;
import com.qacademy.core.dto.EnrollmentDto;
import com.qacademy.core.dto.EnrollmentUpdateDto;
import com.qacademy.core.entity.Course;
import com.qacademy.core.entity.Enrollment;
import com.qacademy.core.entity.Student;
import com.qacademy.core.service.EnrollmentService;
import com.qacademy.infrastructure.repository.CourseRepository;
import com.qacademy.infrastructure.repository.EnrollmentRepository;
import com.qacademy.infrastructure.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// @Transactional at the class level (paired deliberately with
// spring.jpa.open-in-view=false in application.properties): toDto() below reads
// enrollment.getStudent()/getCourse(), which are LAZY associations. Without an
// open transaction spanning the whole method, that read would throw
// LazyInitializationException once open-in-view is off. This is the standard,
// correct pairing - open-in-view=false plus an explicit transaction boundary in
// the service layer, rather than leaving the transaction open for the whole HTTP
// request (which is what open-in-view=true does, and why it's considered an
// anti-pattern at scale).
@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository repository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentServiceImpl(
            EnrollmentRepository repository,
            StudentRepository studentRepository,
            CourseRepository courseRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<EnrollmentDto> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public Optional<EnrollmentDto> getById(Long id) {
        return repository.findById(id).map(this::toDto);
    }

    @Override
    public List<EnrollmentDto> getByStudentId(Long studentId) {
        return repository.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    @Override
    public EnrollmentCreateResult create(EnrollmentCreateDto dto) {
        Optional<Student> studentOpt = studentRepository.findById(dto.studentId());
        if (studentOpt.isEmpty()) {
            return new EnrollmentCreateResult(false, "Student not found", null);
        }

        Optional<Course> courseOpt = courseRepository.findById(dto.courseId());
        if (courseOpt.isEmpty()) {
            return new EnrollmentCreateResult(false, "Course not found", null);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(studentOpt.get());
        enrollment.setCourse(courseOpt.get());
        enrollment.setEnrollmentDate(LocalDateTime.now());

        Enrollment saved = repository.save(enrollment);
        return new EnrollmentCreateResult(true, null, toDto(saved));
    }

    @Override
    public Optional<EnrollmentDto> updateGrade(Long id, EnrollmentUpdateDto dto) {
        Optional<Enrollment> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        Enrollment enrollment = existing.get();
        enrollment.setGrade(dto.grade());

        Enrollment saved = repository.save(enrollment);
        return Optional.of(toDto(saved));
    }

    @Override
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private EnrollmentDto toDto(Enrollment e) {
        Student student = e.getStudent();
        Course course = e.getCourse();
        String studentName = student.getFirstName() + " " + student.getLastName();

        return new EnrollmentDto(
                e.getId(),
                student.getId(),
                studentName,
                course.getId(),
                course.getName(),
                e.getEnrollmentDate(),
                e.getGrade());
    }
}
