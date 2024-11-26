// src/test/java/io/github/emadbytes/codetrack/service/DashboardServiceTest.java
package io.github.emadbytes.codetrack.service;

import io.github.emadbytes.codetrack.dto.DashboardStatsDTO;
import io.github.emadbytes.codetrack.dto.DailyActivityDTO;
import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.SessionStatus;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.CodingSessionRepository;
import io.github.emadbytes.codetrack.service.impl.DashboardServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

        @Mock
        private CodingSessionRepository sessionRepository;

        private DashboardService dashboardService;
        private User testUser;
        private CodingSession testSession;

        @BeforeEach
        void setUp() {
                // Print to verify setup is working
                System.out.println("Starting setUp");

                dashboardService = new DashboardServiceImpl(sessionRepository);

                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");

                testSession = new CodingSession();
                testSession.setId(1L);
                testSession.setUser(testUser);
                testSession.setStartTime(LocalDateTime.now().minusHours(2));
                testSession.setEndTime(LocalDateTime.now().minusHours(1));
                testSession.setStatus(SessionStatus.COMPLETED);
                testSession.setDurationMinutes(60L);
                testSession.setProjectName("Test Project");

                System.out.println("Setup completed");
        }

        @Test
        void whenGetRecentActivity_thenReturnLastSevenDays() {
                // given
                LocalDate today = LocalDate.now();
                LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
                LocalDateTime weekEnd = today.atTime(LocalTime.MAX);

                given(sessionRepository.findByUserAndStartTimeBetween(
                                eq(testUser),
                                eq(weekStart), // Use specific date
                                eq(weekEnd) // Use specific date
                ))
                                .willReturn(Arrays.asList(testSession));

                // when
                List<DailyActivityDTO> activity = dashboardService.getRecentActivity(testUser);

                // then
                assertThat(activity)
                                .isNotEmpty()
                                .hasSize(1);

                DailyActivityDTO dailyActivity = activity.get(0);
                assertThat(dailyActivity.getTotalMinutes()).isEqualTo(60L);
                assertThat(dailyActivity.getSessionCount()).isEqualTo(1L);
                assertThat(dailyActivity.getMainProject()).isEqualTo("Test Project");
        }

        @Test
        void whenGetDetailedStats_thenReturnDateRangeStats() {
                // given
                LocalDate startDate = LocalDate.now().minusDays(7);
                LocalDate endDate = LocalDate.now();

                given(sessionRepository.findByUserAndStartTimeBetween(
                                eq(testUser), any(LocalDateTime.class), any(LocalDateTime.class)))
                                .willReturn(Arrays.asList(testSession));

                // when
                Map<LocalDate, DailyActivityDTO> stats = dashboardService.getDetailedStats(
                                testUser, startDate, endDate);

                // then
                assertThat(stats)
                                .isNotEmpty()
                                .hasSize(1);
        }

        @Test
        void whenGetProjectStats_thenReturnProjectDistribution() {
                // given
                given(sessionRepository.findByUserAndStatus(testUser, SessionStatus.COMPLETED))
                                .willReturn(Arrays.asList(testSession));

                // when
                Map<String, Long> projectStats = dashboardService.getProjectStats(testUser);

                // then
                assertThat(projectStats)
                                .isNotEmpty()
                                .containsKey("Test Project")
                                .containsValue(60L);
        }

        @Test
        void whenCalculateLongestStreak_thenReturnMaxStreak() {
                // given
                given(sessionRepository.findByUser(testUser))
                                .willReturn(Arrays.asList(testSession));

                // when
                Long longestStreak = dashboardService.calculateLongestStreak(testUser);

                // then
                assertThat(longestStreak).isEqualTo(1L);
        }

        @Test
        void whenNoSessions_thenReturnEmptyStats() {
                // given
                given(sessionRepository.findByUserAndStatus(eq(testUser), eq(SessionStatus.COMPLETED)))
                                .willReturn(Arrays.asList());

                // when
                DashboardStatsDTO stats = dashboardService.getDashboardStats(testUser);

                // then
                assertThat(stats).isNotNull();
                assertThat(stats.getTotalSessions()).isEqualTo(0L);
                assertThat(stats.getTotalCodingTime()).isEqualTo(0L);
                assertThat(stats.getCurrentStreak()).isEqualTo(0L);
                assertThat(stats.getProjectTimeDistribution()).isEmpty();
        }
}