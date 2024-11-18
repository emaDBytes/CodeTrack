// src\main\java\io\github\emadbytes\codetrack\service\CodingSessionService.java
package io.github.emadbytes.codetrack.service;

import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing coding sessions.
 * Defines operations that can be performed on coding sessions.
 */
public interface CodingSessionService {

    /**
     * Starts a new coding session for a user.
     *
     * @param user        the user starting the session
     * @param description optional description of the session
     * @param projectName optional project name
     * @return the created coding session
     */
    CodingSession startSession(User user, String description, String projectName);

    /**
     * Ends an active coding session.
     *
     * @param sessionId ID of the session to end
     * @return the updated coding session
     */
    CodingSession endSession(Long sessionId);

    /**
     * Retrieves a specific coding session.
     *
     * @param sessionId ID of the session to retrieve
     * @return the requested coding session
     */
    CodingSession getSession(Long sessionId);

    /**
     * Gets all sessions for a user with pagination.
     *
     * @param user     the user whose sessions to retrieve
     * @param pageable pagination information
     * @return page of coding sessions
     */
    Page<CodingSession> getUserSessions(User user, Pageable pageable);

    /**
     * Gets the current active session for a user if exists.
     *
     * @param user the user whose active session to find
     * @return the active session if exists
     */
    CodingSession getCurrentSession(User user);

    /**
     * Calculates total coding time within a date range.
     *
     * @param user      the user whose time to calculate
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return total minutes spent coding
     */
    Long calculateTotalCodingTime(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets sessions within a specific date range.
     *
     * @param user      the user whose sessions to retrieve
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return list of sessions within the date range
     */
    List<CodingSession> getSessionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate);
}