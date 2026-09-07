package com.qacademy.infrastructure.repository;

import com.qacademy.core.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Spring Data JPA generates the implementation of this interface at runtime -
// this IS the repository pattern in the Java/Spring world (the equivalent
// interview talking point to qEducation/qCampus's "why abstract on top of EF
// Core's DbSet" is "why hand-write a repository when Spring Data already
// generates one from this interface").
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Eager loading via JOIN FETCH - avoids the N+1 problem you'd hit if you
    // lazy-loaded enrollments -> course for every student in a loop. Not wired to
    // any controller endpoint (same as qEducation/qCampus's GetWithEnrollmentsAsync)
    // - kept purely as the eager-loading talking point.
    @Query("select distinct s from Student s left join fetch s.enrollments e left join fetch e.course where s.id = :id")
    Optional<Student> findWithEnrollments(@Param("id") Long id);
}
