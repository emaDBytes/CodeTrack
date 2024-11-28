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

    /**
     * Shows detailed view of a specific session.
     */
    @GetMapping("/{id}")
    public String viewSession(@PathVariable Long id, Model model) {
        log.debug("Fetching session details for ID: {}", id);
        CodingSession session = codingSessionService.getSession(id);
        log.debug("Retrieved session: {}", session);
        CodingSessionDTO sessionDto = CodingSessionDTO.fromEntity(session);
        log.debug("Converted to DTO: {}", sessionDto);
        model.addAttribute("session", sessionDto);
        return "sessions/detail";
    }

    /**
     * Shows the list of user's coding sessions.
     */
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

    /**
     * Shows the form for starting a new session.
     */
    @GetMapping("/start")
    public String showStartForm(Model model) {
        model.addAttribute("newSession", new NewSessionRequest());
        return "sessions/start";
    }

    /**
     * Handles the submission of a new session.
     */
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

    /**
     * Ends the current coding session.
     */
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