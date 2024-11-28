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
import java.time.Duration;
import java.util.List;

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
        log.debug("Starting new session - User: {}, Project: {}, Description: {}",
                user.getUsername(), projectName, description);

        // Check for existing active session
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

        CodingSession savedSession = codingSessionRepository.save(session);
        log.debug("Started new session: {}", savedSession);
        return savedSession;
    }

    @Override
    public CodingSession endSession(Long sessionId) {
        log.debug("Ending session with ID: {}", sessionId);

        CodingSession session = getSession(sessionId);
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new InvalidSessionOperationException("Cannot end a session that is not in progress");
        }

        LocalDateTime endTime = LocalDateTime.now();
        session.setEndTime(endTime);
        session.setStatus(SessionStatus.COMPLETED);

        // Calculate duration in minutes
        Duration duration = Duration.between(session.getStartTime(), endTime);
        session.setDurationMinutes(duration.toMinutes());

        CodingSession savedSession = codingSessionRepository.save(session);
        log.debug("Ended session: {}", savedSession);
        return savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public CodingSession getSession(Long sessionId) {
        log.debug("Fetching session with ID: {}", sessionId);
        return codingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CodingSessionNotFoundException(sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CodingSession> getUserSessions(User user, Pageable pageable) {
        log.debug("Fetching sessions for user: {}, page: {}", user.getUsername(), pageable);
        return codingSessionRepository.findByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CodingSession getCurrentSession(User user) {
        log.debug("Fetching current session for user: {}", user.getUsername());
        return codingSessionRepository
                .findFirstByUserAndStatusOrderByStartTimeDesc(user, SessionStatus.IN_PROGRESS)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Long calculateTotalCodingTime(User user, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculating total coding time for user: {} between {} and {}",
                user.getUsername(), startDate, endDate);
        return codingSessionRepository.calculateTotalDurationByUserAndDateRange(
                user.getId(), startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingSession> getSessionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching sessions for user: {} between {} and {}",
                user.getUsername(), startDate, endDate);
        return codingSessionRepository.findByUserAndStartTimeBetween(user, startDate, endDate);
    }
}