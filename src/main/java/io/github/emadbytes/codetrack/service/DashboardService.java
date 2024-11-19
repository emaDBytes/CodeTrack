// src\main\java\io\github\emadbytes\codetrack\service\DashboardService.java
package io.github.emadbytes.codetrack.service;

import io.github.emadbytes.codetrack.dto.DashboardStatsDTO;
import io.github.emadbytes.codetrack.dto.DailyActivityDTO;
import io.github.emadbytes.codetrack.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for dashboard-related operations.
 * Defines methods for calculating and retrieving various coding statistics.
 */
public interface DashboardService {

    /**
     * Retrieves overall dashboard statistics for a user.
     *
     * @param user the user whose statistics to retrieve
     * @return DTO containing aggregated dashboard statistics
     */
    DashboardStatsDTO getDashboardStats(User user);

    /**
     * Gets recent coding activity for a user.
     * Typically returns the last 7 days of activity.
     *
     * @param user the user whose activity to retrieve
     * @return list of daily activity records
     */
    List<DailyActivityDTO> getRecentActivity(User user);

    /**
     * Retrieves detailed statistics for a specific date range.
     *
     * @param user      the user whose statistics to retrieve
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return map of dates to activity statistics
     */
    Map<LocalDate, DailyActivityDTO> getDetailedStats(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Gets statistics grouped by project.
     *
     * @param user the user whose project statistics to retrieve
     * @return map of project names to total minutes spent
     */
    Map<String, Long> getProjectStats(User user);

    /**
     * Calculates the current coding streak (consecutive days with activity).
     *
     * @param user the user whose streak to calculate
     * @return number of consecutive days with coding activity
     */
    Long calculateCurrentStreak(User user);

    /**
     * Calculates the longest coding streak ever achieved.
     *
     * @param user the user whose streak to calculate
     * @return the longest streak of consecutive coding days
     */
    Long calculateLongestStreak(User user);
}