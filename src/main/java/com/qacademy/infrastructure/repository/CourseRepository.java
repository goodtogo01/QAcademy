package com.qacademy.infrastructure.repository;

import com.qacademy.core.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
