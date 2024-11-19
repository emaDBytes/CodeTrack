// src\main\java\io\github\emadbytes\codetrack\dto\DashboardStatsDTO.java
package io.github.emadbytes.codetrack.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * Data Transfer Object for dashboard statistics.
 * This class carries aggregated coding statistics from the service layer to the
 * view.
 * It contains only the data needed for display, separated from the entity
 * model.
 */
@Data
public class DashboardStatsDTO {

    /**
     * Total minutes spent coding across all sessions
     */
    private Long totalCodingTime;

    /**
     * Number of completed coding sessions
     */
    private Long totalSessions;

    /**
     * Average session duration in minutes
     */
    private Long averageSessionDuration;

    /**
     * Number of consecutive days with coding activity
     */
    private Long currentStreak;

    /**
     * Longest streak of consecutive coding days
     */
    private Long longestStreak;

    /**
     * Daily coding time for the last 7 days
     * Key: Date, Value: Minutes spent coding
     */
    private Map<LocalDate, Long> lastSevenDaysActivity;

    /**
     * Projects worked on and time spent on each
     * Key: Project name, Value: Minutes spent
     */
    private Map<String, Long> projectTimeDistribution;

    /**
     * Most productive hour of the day (0-23)
     */
    private Integer mostProductiveHour;

    /**
     * Total coding time for current month in minutes
     */
    private Long currentMonthTotal;

    /**
     * Daily average for the current month in minutes
     */
    private Long currentMonthDailyAverage;

}
