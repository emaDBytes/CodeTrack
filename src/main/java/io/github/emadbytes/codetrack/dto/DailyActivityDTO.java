// src\main\java\io\github\emadbytes\codetrack\dto\DailyActivityDTO.java
package io.github.emadbytes.codetrack.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Data Transfer Object for daily coding activity.
 * Used to represent coding statistics for a single day.
 */
@Data
public class DailyActivityDTO {

    /**
     * The date of the activity
     */
    private LocalDate date;

    /**
     * Total minutes spent coding on this date
     */
    private Long totalMinutes;

    /**
     * Number of sessions completed on this date
     */
    private Long sessionCount;

    /**
     * Indicates if any coding was done on this date
     */
    private boolean hasActivity;

    /**
     * Most worked on project for this date
     */
    private String mainProject;
}