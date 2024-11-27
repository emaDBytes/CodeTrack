// src/main/java/io/github/emadbytes/codetrack/config/DevDataConfig.java
package io.github.emadbytes.codetrack.config;

import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.CodingSessionRepository;
import io.github.emadbytes.codetrack.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@Profile("dev")
@Slf4j
public class DevDataConfig {

    private final Random random = new Random();

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            CodingSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            log.info("Starting database initialization...");

            // Create users
            List<User> users = createUsers(passwordEncoder);
            userRepository.saveAll(users);
            log.info("Created {} users", users.size());

            // Create sessions for each user
            for (User user : users) {
                if (!user.getUsername().equals("admin")) { // Skip admin user for sessions
                    List<CodingSession> sessions = createUserSessions(user);
                    sessionRepository.saveAll(sessions);
                    log.info("Created {} sessions for user {}", sessions.size(), user.getUsername());
                }
            }

            log.info("Database initialization completed!");
        };
    }

    private List<User> createUsers(PasswordEncoder passwordEncoder) {
        List<User> users = new ArrayList<>();

        // Create admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@codetrack.local");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setActive(true);
        admin.addRole(Role.ADMIN);
        users.add(admin);

        // Create regular users
        String[] usernames = { "john", "alice", "bob" };
        for (String username : usernames) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(username + "@codetrack.local");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setActive(true);
            user.addRole(Role.USER);
            users.add(user);
        }

        return users;
    }

    private List<CodingSession> createUserSessions(User user) {
        List<CodingSession> sessions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        String[] projects = {
                "Spring Boot Project",
                "React Frontend",
                "Database Design",
                "Algorithm Practice",
                "Testing Practice"
        };

        // Create sessions for the last 30 days
        for (int i = 30; i >= 0; i--) {
            // 80% chance to have sessions on a day
            if (random.nextDouble() > 0.8)
                continue;

            // 1-3 sessions per day
            int sessionsPerDay = random.nextInt(3) + 1;
            for (int j = 0; j < sessionsPerDay; j++) {
                CodingSession session = new CodingSession();
                session.setUser(user);

                // Base time at 9 AM plus some hours
                LocalDateTime startTime = now.minusDays(i)
                        .withHour(9 + j * 3)
                        .withMinute(random.nextInt(60));
                session.setStartTime(startTime);

                // Duration between 30 minutes and 3 hours
                int durationMinutes = 30 + random.nextInt(150);
                session.setEndTime(startTime.plusMinutes(durationMinutes));
                session.setDurationMinutes((long) durationMinutes);

                session.setProjectName(projects[random.nextInt(projects.length)]);
                session.setDescription(getRandomDescription());
                session.setStatus(SessionStatus.COMPLETED);

                sessions.add(session);
            }
        }

        // Add one active session (30% chance)
        if (random.nextDouble() < 0.3) {
            CodingSession activeSession = new CodingSession();
            activeSession.setUser(user);
            activeSession.setStartTime(LocalDateTime.now().minusMinutes(random.nextInt(60)));
            activeSession.setProjectName(projects[random.nextInt(projects.length)]);
            activeSession.setDescription("Working on current tasks");
            activeSession.setStatus(SessionStatus.IN_PROGRESS);
            sessions.add(activeSession);
        }

        return sessions;
    }

    private String getRandomDescription() {
        String[] descriptions = {
                "Implementing new features",
                "Fixing bugs",
                "Writing unit tests",
                "Code refactoring",
                "Adding documentation",
                "Performance optimization",
                "Learning new concepts",
                "Working on assignments"
        };
        return descriptions[random.nextInt(descriptions.length)];
    }
}