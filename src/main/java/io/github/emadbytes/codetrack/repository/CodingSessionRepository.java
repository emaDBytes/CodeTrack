// src\main\java\io\github\emadbytes\codetrack\repository\CodingSessionRepository.java
package io.github.emadbytes.codetrack.repository;

import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CodingSession entity.
 * Provides methods to interact with coding_sessions table in the database.
 */
public interface CodingSessionRepository extends JpaRepository<CodingSession, Long> {

    /**
     * Finds all coding sessions for a specific user.
     *
     * @param user the user whose sessions to find
     * @return list of coding sessions
     */
    List<CodingSession> findByUser(User user);

    /**
     * Finds all coding sessions for a user with pagination.
     *
     * @param user     user whose sessions to find
     * @param pageable pagination information
     * @return page of coding sessions
     */
    Page<CodingSession> findByUser(User user, Pageable pageable);

    /**
     * Finds active (IN_PROGRESS) sessions for a user.
     *
     * @param user   the user whose active sessions to find
     * @param status the session status to find (typically IN_PROGRESS)
     * @return list of active coding sessions
     */
    List<CodingSession> findByUserAndStatus(User user, SessionStatus status);

    /**
     * Finds sessions within a date range for a specific user.
     *
     * @param user      the user whose sessions to find
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return list of coding sessions within the date range
     */
    List<CodingSession> findByUserAndStartTimeBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculates total coding duration for a user within a date range.
     *
     * @param userId    the user's ID
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return total duration in minutes
     */
    @Query("SELECT COALESCE(SUM(cs.durationMinutes), 0) FROM CodingSession cs " +
            "WHERE cs.user.id = :userId " +
            "AND cs.status = 'COMPLETED' " +
            "AND cs.startTime BETWEEN :startDate AND :endDate")
    Long calculateTotalDurationByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds the latest active session for a user.
     *
     * @param user   the user whose active session to find
     * @param status the session status (typically IN_PROGRESS)
     * @return optional containing the latest active session if exists
     */
    Optional<CodingSession> findFirstByUserAndStatusOrderByStartTimeDesc(User user, SessionStatus status);
}