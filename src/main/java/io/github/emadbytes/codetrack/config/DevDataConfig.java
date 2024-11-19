// src\main\java\io\github\emadbytes\codetrack\config\DevDataConfig.java
package io.github.emadbytes.codetrack.config;

import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.service.CodingSessionService;
import io.github.emadbytes.codetrack.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Configuration class for seeding development data.
 * Only active when the "dev" profile is enabled.
 */
@Configuration
@Profile("dev")
@Slf4j
public class DevDataConfig {

    private final Random random = new Random();

    /**
     * Creates sample data for development environment.
     */
    @Bean
    public CommandLineRunner seedData(
            UserService userService,
            CodingSessionService sessionService,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Seeding development data...");

            // Create sample users
            createSampleUsers(userService, passwordEncoder);

            // Create sample coding sessions
            createSampleSessions(userService, sessionService);

            log.info("Development data seeding completed.");
        };
    }

    /**
     * Creates sample users with different roles.
     */
    private void createSampleUsers(UserService userService, PasswordEncoder passwordEncoder) {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@codetrack.local");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setActive(true);
        admin.addRole(Role.ADMIN);
        userService.createUser(admin);

        // Create regular users
        List<String> users = Arrays.asList("john", "alice", "bob");
        for (String username : users) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(username + "@codetrack.local");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setActive(true);
            user.addRole(Role.USER);
            userService.createUser(user);
        }
    }

    /**
     * Creates sample coding sessions with realistic patterns.
     */
    private void createSampleSessions(UserService userService, CodingSessionService sessionService) {
        // Get demo user
        User john = userService.getUserByUsername("john")
                .orElseThrow(() -> new RuntimeException("Demo user not found"));

        // Sample projects
        List<String> projects = Arrays.asList(
                "Spring Boot Project",
                "React Frontend",
                "Database Design",
                "Algorithm Practice");

        // Create sessions for the last 30 days
        LocalDateTime now = LocalDateTime.now();
        for (int i = 30; i >= 0; i--) {
            // Skip some days randomly to create gaps
            if (random.nextDouble() < 0.2) { // 20% chance to skip a day
                continue;
            }

            // Create 1-3 sessions per day
            int sessionsPerDay = random.nextInt(3) + 1;
            LocalDateTime baseTime = now.minusDays(i).withHour(9); // Start at 9 AM

            for (int j = 0; j < sessionsPerDay; j++) {
                createSession(john, baseTime.plusHours(j * 3), projects);
            }
        }

        // Create one active session
        if (random.nextBoolean()) { // 50% chance to have an active session
            CodingSession activeSession = new CodingSession();
            activeSession.setUser(john);
            activeSession.setStartTime(LocalDateTime.now().minusHours(1));
            activeSession.setStatus(SessionStatus.IN_PROGRESS);
            activeSession.setProjectName(projects.get(random.nextInt(projects.size())));
            activeSession.setDescription("Working on new features");
        }
    }

    /**
     * Creates a single coding session with random duration and project.
     */
    private void createSession(User user, LocalDateTime startTime, List<String> projects) {
        CodingSession session = new CodingSession();
        session.setUser(user);
        session.setStartTime(startTime);

        // Random duration between 30 minutes and 3 hours
        int durationMinutes = 30 + random.nextInt(150);
        session.setEndTime(startTime.plusMinutes(durationMinutes));
        session.setDurationMinutes((long) durationMinutes);

        session.setStatus(SessionStatus.COMPLETED);
        session.setProjectName(projects.get(random.nextInt(projects.size())));

        // Add some description
        List<String> activities = Arrays.asList(
                "Implementing new features",
                "Fixing bugs",
                "Writing tests",
                "Code refactoring",
                "Documentation updates");
        session.setDescription(activities.get(random.nextInt(activities.size())));
    }
}