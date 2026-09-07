-- qAcademy — School Management API
-- Flyway migration: the Java/Maven equivalent of qCampus's EF Core migration.
-- Column names/types here must match the @Column mappings in the JPA entities
-- exactly, since spring.jpa.hibernate.ddl-auto=validate checks them at startup.

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    credits INT NOT NULL
);

CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_of_birth VARCHAR(10) NOT NULL
);

CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date TIMESTAMP NOT NULL,
    grade VARCHAR(10),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    student_id BIGINT,
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

-- Seed data so Swagger has something to query immediately (mirrors qEducation/qCampus).
INSERT INTO courses (name, credits) VALUES ('Algebra I', 3);
INSERT INTO courses (name, credits) VALUES ('US History', 3);

INSERT INTO students (first_name, last_name, email, date_of_birth)
VALUES ('Ada', 'Lovelace', 'ada@qacademy.test', '10/12/2008');

-- Login users are NOT seeded here (same reasoning as qEducation/qCampus): password
-- hashes need BCrypt computed at runtime, not baked into a migration file. See
-- DataSeeder.java, which runs once via CommandLineRunner after this migration.
