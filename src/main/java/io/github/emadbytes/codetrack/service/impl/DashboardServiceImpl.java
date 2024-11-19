// src\main\java\io\github\emadbytes\codetrack\service\impl\DashboardServiceImpl.java
package io.github.emadbytes.codetrack.service.impl;

import io.github.emadbytes.codetrack.dto.DashboardStatsDTO;
import io.github.emadbytes.codetrack.dto.DailyActivityDTO;
import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.CodingSessionRepository;
import io.github.emadbytes.codetrack.service.DashboardService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the DashboardService interface.
 * Provides calculations and statistics for the dashboard.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final CodingSessionRepository sessionRepository;

    public DashboardServiceImpl(CodingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats(User user) {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDateTime.now();

        // Calculate basic statistics
        stats.setTotalSessions(countCompletedSessions(user));
        stats.setTotalCodingTime(calculateTotalCodingTime(user));
        stats.setCurrentStreak(calculateCurrentStreak(user));
        stats.setLongestStreak(calculateLongestStreak(user));

        // Calculate averages
        if (stats.getTotalSessions() > 0) {
            stats.setAverageSessionDuration(stats.getTotalCodingTime() / stats.getTotalSessions());
        }

        // Get current month statistics
        stats.setCurrentMonthTotal(calculateTotalCodingTimeInRange(user, monthStart, monthEnd));
        long daysInCurrentMonth = monthEnd.getDayOfMonth();
        if (daysInCurrentMonth > 0) {
            stats.setCurrentMonthDailyAverage(stats.getCurrentMonthTotal() / daysInCurrentMonth);
        }

        // Get last 7 days activity
        stats.setLastSevenDaysActivity(getLastSevenDaysActivity(user));

        // Calculate project distribution
        stats.setProjectTimeDistribution(calculateProjectDistribution(user));

        // Find most productive hour
        stats.setMostProductiveHour(findMostProductiveHour(user));

        return stats;
    }

    @Override
    public List<DailyActivityDTO> getRecentActivity(User user) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Last 7 days including today

        return sessionRepository.findByUserAndStartTimeBetween(
                user,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX))
                .stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStartTime().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createDailyActivityFromSessions)))
                .values()
                .stream()
                .sorted(Comparator.comparing(DailyActivityDTO::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Map<LocalDate, DailyActivityDTO> getDetailedStats(User user, LocalDate startDate, LocalDate endDate) {
        List<CodingSession> sessions = sessionRepository.findByUserAndStartTimeBetween(
                user,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX));

        return sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStartTime().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createDailyActivityFromSessions)));
    }

    @Override
    public Map<String, Long> getProjectStats(User user) {
        List<CodingSession> completedSessions = sessionRepository.findByUserAndStatus(user, SessionStatus.COMPLETED);

        return completedSessions.stream()
                .filter(session -> session.getProjectName() != null && !session.getProjectName().isEmpty())
                .collect(Collectors.groupingBy(
                        CodingSession::getProjectName,
                        Collectors.summingLong(
                                session -> session.getDurationMinutes() != null ? session.getDurationMinutes() : 0L)));
    }

    @Override
    public Long calculateCurrentStreak(User user) {
        LocalDate currentDate = LocalDate.now();
        long streak = 0;

        while (true) {
            if (hasActivityOnDate(user, currentDate)) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    @Override
    public Long calculateLongestStreak(User user) {
        List<CodingSession> allSessions = sessionRepository.findByUser(user);
        if (allSessions.isEmpty()) {
            return 0L;
        }

        Set<LocalDate> activeDates = allSessions.stream()
                .map(session -> session.getStartTime().toLocalDate())
                .collect(Collectors.toSet());

        long maxStreak = 0;
        long currentStreak = 0;
        LocalDate currentDate = Collections.min(activeDates);
        LocalDate endDate = LocalDate.now();

        while (!currentDate.isAfter(endDate)) {
            if (activeDates.contains(currentDate)) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            currentDate = currentDate.plusDays(1);
        }

        return maxStreak;
    }

    // Helper methods

    private Long countCompletedSessions(User user) {
        return sessionRepository.findByUserAndStatus(user, SessionStatus.COMPLETED).stream().count();
    }

    private Long calculateTotalCodingTime(User user) {
        return sessionRepository.findByUserAndStatus(user, SessionStatus.COMPLETED).stream()
                .mapToLong(session -> session.getDurationMinutes() != null ? session.getDurationMinutes() : 0L)
                .sum();
    }

    private Long calculateTotalCodingTimeInRange(User user, LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findByUserAndStartTimeBetween(user, start, end).stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .mapToLong(session -> session.getDurationMinutes() != null ? session.getDurationMinutes() : 0L)
                .sum();
    }

    /**
     * Calculates the distribution of time spent across different projects.
     *
     * @param user the user whose project distribution to calculate
     * @return map of project names to total minutes spent
     */
    private Map<String, Long> calculateProjectDistribution(User user) {
        List<CodingSession> completedSessions = sessionRepository.findByUserAndStatus(user, SessionStatus.COMPLETED);

        Map<String, Long> distribution = completedSessions.stream()
                .filter(session -> session.getDurationMinutes() != null)
                .collect(Collectors.groupingBy(
                        // Use "Unspecified" if project name is null or empty
                        session -> session.getProjectName() != null && !session.getProjectName().isEmpty()
                                ? session.getProjectName()
                                : "Unspecified",
                        Collectors.summingLong(CodingSession::getDurationMinutes)));

        // If there are any sessions but no projects specified, add "Unspecified" with 0
        // minutes
        if (distribution.isEmpty() && !completedSessions.isEmpty()) {
            distribution.put("Unspecified", 0L);
        }

        return distribution;
    }

    private Map<LocalDate, Long> getLastSevenDaysActivity(User user) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        Map<LocalDate, Long> activityMap = new LinkedHashMap<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            activityMap.put(current, 0L);
            current = current.plusDays(1);
        }

        List<CodingSession> sessions = sessionRepository.findByUserAndStartTimeBetween(
                user,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX));

        sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .forEach(session -> {
                    LocalDate sessionDate = session.getStartTime().toLocalDate();
                    activityMap.merge(sessionDate,
                            session.getDurationMinutes() != null ? session.getDurationMinutes() : 0L,
                            Long::sum);
                });

        return activityMap;
    }

    private Integer findMostProductiveHour(User user) {
        Map<Integer, Long> hourlyActivity = sessionRepository.findByUserAndStatus(user, SessionStatus.COMPLETED)
                .stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStartTime().getHour(),
                        Collectors.summingLong(
                                session -> session.getDurationMinutes() != null ? session.getDurationMinutes() : 0L)));

        return hourlyActivity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private boolean hasActivityOnDate(User user, LocalDate date) {
        return sessionRepository.findByUserAndStartTimeBetween(
                user,
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX))
                .stream()
                .anyMatch(session -> session.getStatus() == SessionStatus.COMPLETED);
    }

    private DailyActivityDTO createDailyActivityFromSessions(List<CodingSession> sessions) {
        DailyActivityDTO activity = new DailyActivityDTO();
        activity.setDate(sessions.get(0).getStartTime().toLocalDate());
        activity.setSessionCount((long) sessions.size());
        activity.setTotalMinutes(sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .mapToLong(session -> session.getDurationMinutes() != null ? session.getDurationMinutes() : 0L)
                .sum());
        activity.setHasActivity(activity.getTotalMinutes() > 0);

        // Find most worked on project
        activity.setMainProject(sessions.stream()
                .filter(session -> session.getProjectName() != null && !session.getProjectName().isEmpty())
                .collect(Collectors.groupingBy(
                        CodingSession::getProjectName,
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No Project"));

        return activity;
    }
}