// src\main\java\io\github\emadbytes\codetrack\model\CodingSession.java
package io.github.emadbytes.codetrack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity class representing a coding session in the CodeTrack application.
 * Tracks when a user starts and ends their coding activities.
 * This class maps to the 'coding_sessions' table in the database.
 */
@Entity
@Table(name = "coding_sessions")
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok: Generates a no-args constructor required by JPA
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
public class CodingSession {

    /**
     * Unique identifier for the coding session.
     * Auto-generated when a new session is persisted.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this coding session.
     * Many sessions can belong to one user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The timestamp when the coding session started.
     * Required field that cannot be null.
     */
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    /**
     * The timestamp when the coding session ended.
     * Null while the session is ongoing.
     */
    private LocalDateTime endTime;

    /**
     * Optional description of what was worked on during the session.
     * Limited to 500 characters.
     */
    @Column(length = 500)
    private String description;

    /**
     * Name of the project being worked on.
     * Optional field to categorize coding sessions.
     */
    private String projectName;

    /**
     * Current status of the coding session.
     * Defaults to IN_PROGRESS when created.
     */
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    /**
     * Duration of the session in minutes.
     * Calculated and stored when the session is completed.
     */
    @Column(name = "duration_minutes")
    private Long durationMinutes;

    /**
     * Automatically calculates and updates the session duration
     * when the session is completed and has an end time.
     */
    @PreUpdate
    protected void onUpdate() {
        if (endTime != null && status == SessionStatus.COMPLETED) {
            this.durationMinutes = Duration.between(startTime, endTime).toMinutes();
        }
    }
}