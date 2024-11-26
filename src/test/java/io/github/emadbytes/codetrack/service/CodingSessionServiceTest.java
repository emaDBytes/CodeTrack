// src/test/java/io/github/emadbytes/codetrack/service/CodingSessionServiceTest.java
package io.github.emadbytes.codetrack.service;

import io.github.emadbytes.codetrack.exception.CodingSessionNotFoundException;
import io.github.emadbytes.codetrack.exception.InvalidSessionOperationException;
import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.CodingSessionRepository;
import io.github.emadbytes.codetrack.service.impl.CodingSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CodingSessionServiceTest {

    @Mock
    private CodingSessionRepository sessionRepository;

    private CodingSessionService sessionService;
    private User testUser;
    private CodingSession testSession;

    @BeforeEach
    void setUp() {
        sessionService = new CodingSessionServiceImpl(sessionRepository);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Create test session
        testSession = new CodingSession();
        testSession.setId(1L);
        testSession.setUser(testUser);
        testSession.setStartTime(LocalDateTime.now());
        testSession.setStatus(SessionStatus.IN_PROGRESS);
        testSession.setProjectName("Test Project");
    }

    @Test
    void whenStartSession_withNoActiveSessions_thenSucceed() {
        // given
        given(sessionRepository.findByUserAndStatus(testUser, SessionStatus.IN_PROGRESS))
                .willReturn(Collections.emptyList());
        given(sessionRepository.save(any(CodingSession.class))).willReturn(testSession);

        // when
        CodingSession started = sessionService.startSession(testUser, "Test session", "Test Project");

        // then
        assertThat(started).isNotNull();
        assertThat(started.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(started.getUser()).isEqualTo(testUser);
        verify(sessionRepository).save(any(CodingSession.class));
    }

    @Test
    void whenStartSession_withExistingActiveSession_thenThrowException() {
        // given
        given(sessionRepository.findByUserAndStatus(testUser, SessionStatus.IN_PROGRESS))
                .willReturn(Collections.singletonList(testSession));

        // when/then
        assertThatThrownBy(() -> sessionService.startSession(testUser, "Test session", "Test Project"))
                .isInstanceOf(InvalidSessionOperationException.class)
                .hasMessageContaining("already has an active coding session");
    }

    @Test
    void whenEndSession_withActiveSession_thenSucceed() {
        // given
        given(sessionRepository.findById(1L)).willReturn(Optional.of(testSession));
        given(sessionRepository.save(any(CodingSession.class))).willReturn(testSession);

        // when
        CodingSession ended = sessionService.endSession(1L);

        // then
        assertThat(ended).isNotNull();
        assertThat(ended.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(ended.getEndTime()).isNotNull();
    }

    @Test
    void whenEndSession_withCompletedSession_thenThrowException() {
        // given
        testSession.setStatus(SessionStatus.COMPLETED);
        given(sessionRepository.findById(1L)).willReturn(Optional.of(testSession));

        // when/then
        assertThatThrownBy(() -> sessionService.endSession(1L))
                .isInstanceOf(InvalidSessionOperationException.class)
                .hasMessageContaining("not in progress");
    }

    @Test
    void whenGetSession_withExistingId_thenReturnSession() {
        // given
        given(sessionRepository.findById(1L)).willReturn(Optional.of(testSession));

        // when
        CodingSession found = sessionService.getSession(1L);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetSession_withNonExistingId_thenThrowException() {
        // given
        given(sessionRepository.findById(1L)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> sessionService.getSession(1L))
                .isInstanceOf(CodingSessionNotFoundException.class);
    }

    @Test
    void whenGetUserSessions_thenReturnPagedResults() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<CodingSession> sessionPage = new PageImpl<>(Arrays.asList(testSession));
        given(sessionRepository.findByUser(testUser, pageable)).willReturn(sessionPage);

        // when
        Page<CodingSession> result = sessionService.getUserSessions(testUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testSession);
    }

    @Test
    void whenGetCurrentSession_withActiveSession_thenReturnSession() {
        // given
        given(sessionRepository.findFirstByUserAndStatusOrderByStartTimeDesc(
                testUser, SessionStatus.IN_PROGRESS))
                .willReturn(Optional.of(testSession));

        // when
        CodingSession current = sessionService.getCurrentSession(testUser);

        // then
        assertThat(current).isNotNull();
        assertThat(current.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    }

    @Test
    void whenCalculateTotalCodingTime_thenReturnTotal() {
        // given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        given(sessionRepository.calculateTotalDurationByUserAndDateRange(
                testUser.getId(), start, end))
                .willReturn(120L); // 120 minutes

        // when
        Long total = sessionService.calculateTotalCodingTime(testUser, start, end);

        // then
        assertThat(total).isEqualTo(120L);
    }

    @Test
    void whenGetSessionsByDateRange_thenReturnFilteredSessions() {
        // given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<CodingSession> sessions = Arrays.asList(testSession);
        given(sessionRepository.findByUserAndStartTimeBetween(testUser, start, end))
                .willReturn(sessions);

        // when
        List<CodingSession> result = sessionService.getSessionsByDateRange(testUser, start, end);

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .contains(testSession);
    }
}