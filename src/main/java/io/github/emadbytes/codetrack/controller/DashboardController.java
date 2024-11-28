// src\main\java\io\github\emadbytes\codetrack\controller\DashboardController.java
package io.github.emadbytes.codetrack.controller;

import io.github.emadbytes.codetrack.dto.DashboardStatsDTO;
import io.github.emadbytes.codetrack.dto.DailyActivityDTO;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.service.CodingSessionService;
import io.github.emadbytes.codetrack.service.DashboardService;
import io.github.emadbytes.codetrack.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller responsible for handling dashboard-related requests.
 * Provides views for coding statistics and activity summaries.
 */
@Controller
@RequestMapping("/dashboard")
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final DashboardService dashboardService;
    private final CodingSessionService codingSessionService;

    /**
     * Constructor injection of required services.
     * 
     * @param userService          service for user-related operations
     * @param dashboardService     service for dashboard statistics
     * @param codingSessionService service for coding session operations
     */
    public DashboardController(UserService userService,
            DashboardService dashboardService,
            CodingSessionService codingSessionService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.codingSessionService = codingSessionService;
    }

    @Operation(summary = "Display the main dashboard page with overall statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data successfully retrieved", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get dashboard statistics
        DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
        model.addAttribute("stats", stats);

        // Get current/active session if exists
        model.addAttribute("currentSession", codingSessionService.getCurrentSession(user));

        // Get recent activity
        List<DailyActivityDTO> recentActivity = dashboardService.getRecentActivity(user);
        model.addAttribute("recentActivity", recentActivity);

        return "dashboard/index";
    }

    @Operation(summary = "Provide detailed statistics for a specific date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detailed statistics successfully retrieved", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/stats")
    public String showDetailedStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If dates not provided, default to last 30 days
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        model.addAttribute("detailedStats",
                dashboardService.getDetailedStats(user, startDate, endDate));

        return "dashboard/detailed-stats";
    }

    @Operation(summary = "Provide project-specific statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project statistics successfully retrieved", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/projects")
    public String showProjectStats(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("projectStats", dashboardService.getProjectStats(user));
        return "dashboard/project-stats";
    }
}