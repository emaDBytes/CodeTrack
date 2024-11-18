// src\main\java\io\github\emadbytes\codetrack\service\impl\CodingSessionServiceImpl.java
package io.github.emadbytes.codetrack.service.impl;

import io.github.emadbytes.codetrack.exception.CodingSessionNotFoundException;
import io.github.emadbytes.codetrack.exception.InvalidSessionOperationException;
import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.CodingSessionRepository;
import io.github.emadbytes.codetrack.service.CodingSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the CodingSessionService interface.
 * Provides business logic for managing coding sessions.
 */
@Service
@Transactional
@Slf4j
public class CodingSessionServiceImpl implements CodingSessionService {

    private final CodingSessionRepository codingSessionRepository;

    public CodingSessionServiceImpl(CodingSessionRepository codingSessionRepository) {
        this.codingSessionRepository = codingSessionRepository;
    }

    @Override
    public CodingSession startSession(User user, String description, String projectName) {
        // Check if user has any active sessions
        List<CodingSession> activeSessions = codingSessionRepository
                .findByUserAndStatus(user, SessionStatus.IN_PROGRESS);

        if (!activeSessions.isEmpty()) {
            throw new InvalidSessionOperationException("User already has an active coding session");
        }

        CodingSession session = new CodingSession();
        session.setUser(user);
        session.setStartTime(LocalDateTime.now());
        session.setDescription(description);
        session.setProjectName(projectName);
        session.setStatus(SessionStatus.IN_PROGRESS);

        log.info("Starting new coding session for user: {}", user.getUsername());
        return codingSessionRepository.save(session);
    }

    @Override
    public CodingSession endSession(Long sessionId) {
        CodingSession session = getSession(sessionId);

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new InvalidSessionOperationException("Cannot end a session that is not in progress");
        }

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.COMPLETED);

        log.info("Ending coding session: {}", sessionId);
        return codingSessionRepository.save(session);
    }

    @Override
    public CodingSession getSession(Long sessionId) {
        return codingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CodingSessionNotFoundException(sessionId));
    }

    @Override
    public Page<CodingSession> getUserSessions(User user, Pageable pageable) {
        return codingSessionRepository.findByUser(user, pageable);
    }

    @Override
    public CodingSession getCurrentSession(User user) {
        return codingSessionRepository
                .findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.IN_PROGRESS)
                .orElse(null);
    }

    @Override
    public Long calculateTotalCodingTime(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return codingSessionRepository.calculateTotalDurationByUserAndDateRange(
                user.getId(), startDate, endDate);
    }

    @Override
    public List<CodingSession> getSessionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return codingSessionRepository.findByUserAndStartTimeBetween(user, startDate, endDate);
    }
}