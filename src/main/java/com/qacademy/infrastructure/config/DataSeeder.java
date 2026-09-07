package com.qacademy.infrastructure.config;

import com.qacademy.core.entity.User;
import com.qacademy.core.entity.UserRole;
import com.qacademy.infrastructure.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Runs once on startup, after Flyway migrations have created the schema - the Java
// equivalent of qEducation/qCampus's DbInitializer.SeedAsync. Login users aren't
// seeded via SQL because their password hashes need BCrypt computed at runtime.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "Admin123!", UserRole.ADMIN);
        seedUser("staff", "Admin123!", UserRole.STAFF);
        seedUser("student", "Admin123!", UserRole.STUDENT);
    }

    private void seedUser(String username, String rawPassword, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }
}
