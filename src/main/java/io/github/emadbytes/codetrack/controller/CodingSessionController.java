// src\main\java\io\github\emadbytes\codetrack\controller\CodingSessionController.java
package io.github.emadbytes.codetrack.controller;

import io.github.emadbytes.codetrack.dto.CodingSessionDTO;
import io.github.emadbytes.codetrack.dto.NewSessionRequest;
import io.github.emadbytes.codetrack.exception.UserNotFoundException;
import io.github.emadbytes.codetrack.model.CodingSession;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.service.CodingSessionService;
import io.github.emadbytes.codetrack.service.UserService;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller for managing coding sessions.
 * Handles web requests related to coding session management.
 */
@Controller
@RequestMapping("/sessions")
@Slf4j
public class CodingSessionController {

    private final CodingSessionService codingSessionService;
    private final UserService userService;

    public CodingSessionController(CodingSessionService codingSessionService, UserService userService) {
        this.codingSessionService = codingSessionService;
        this.userService = userService;
    }

    @Operation(summary = "Show detailed view of a specific coding session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coding session details successfully retrieved", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Coding session not found", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{id}")
    public String viewSession(@PathVariable Long id, Model model) {
        CodingSession rawSession = codingSessionService.getSession(id);
        CodingSessionDTO sessionDto = CodingSessionDTO.fromEntity(rawSession);

        // Use a different name to avoid conflict with HttpSession
        model.addAttribute("codingSession", sessionDto);
        model.addAttribute("rawSession", rawSession);

        log.debug("Added to model - codingSession: {}", sessionDto);

        return "sessions/detail";
    }

    @Operation(summary = "Show the list of user's coding sessions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coding sessions successfully retrieved", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping
    public String listSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        Page<CodingSession> sessions = codingSessionService.getUserSessions(
                user, PageRequest.of(page, size));

        model.addAttribute("sessions", sessions.map(CodingSessionDTO::fromEntity));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sessions.getTotalPages());

        // Add current session if exists
        CodingSession currentSession = codingSessionService.getCurrentSession(user);
        if (currentSession != null) {
            model.addAttribute("currentSession", CodingSessionDTO.fromEntity(currentSession));
        }

        return "sessions/list";
    }

    @Operation(summary = "Show the form for starting a new coding session")
    @GetMapping("/start")
    public String showStartForm(Model model) {
        model.addAttribute("newSession", new NewSessionRequest());
        return "sessions/start";
    }

    @Operation(summary = "Start a new coding session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Coding session started successfully", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid session data", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/start")
    public String startSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("newSession") NewSessionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "sessions/start";
        }

        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
            CodingSession session = codingSessionService.startSession(
                    user, request.getDescription(), request.getProjectName());

            redirectAttributes.addFlashAttribute("success",
                    "Coding session started successfully! Session ID: " + session.getId());
            return "redirect:/sessions";

        } catch (Exception e) {
            log.error("Error starting session", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sessions/start";
        }
    }

    @Operation(summary = "End the current coding session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Coding session ended successfully", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Error ending session", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/{id}/end")
    public String endSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            codingSessionService.endSession(id);
            redirectAttributes.addFlashAttribute("success", "Coding session ended successfully!");
        } catch (Exception e) {
            log.error("Error ending session", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/sessions";
    }
}