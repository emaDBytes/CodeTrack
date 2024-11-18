// src\main\java\io\github\emadbytes\codetrack\dto\CodingSessionDTO.java
package io.github.emadbytes.codetrack.dto;

import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.SessionStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for CodingSession entity.
 * Used for transferring coding session data between layers without exposing
 * entity details.
 */
@Data
@NoArgsConstructor
public class CodingSessionDTO {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String projectName;
    private SessionStatus status;
    private Long durationMinutes;
    private String formattedDuration;

    /**
     * Converts a CodingSession entity to its DTO representation.
     *
     * @param session the coding session entity
     * @return DTO representation of the coding session
     */
    public static CodingSessionDTO fromEntity(CodingSession session) {
        CodingSessionDTO dto = new CodingSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUser().getId());
        dto.setUsername(session.getUser().getUsername());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setDescription(session.getDescription());
        dto.setProjectName(session.getProjectName());
        dto.setStatus(session.getStatus());
        dto.setDurationMinutes(session.getDurationMinutes());

        if (session.getDurationMinutes() != null) {
            long hours = session.getDurationMinutes() / 60;
            long minutes = session.getDurationMinutes() % 60;
            dto.setFormattedDuration(String.format("%dh %dm", hours, minutes));
        }

        return dto;
    }
}