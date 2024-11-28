// src\main\java\io\github\emadbytes\codetrack\controller\UserController.java
package io.github.emadbytes.codetrack.controller;

import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller handling user registration and profile management.
 */
@Controller
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Show the user registration form")
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
        log.debug("Received registration request for username: {}", user.getUsername());

        // Validate username and email uniqueness
        if (!userService.isUsernameAvailable(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "users/register";
        }

        if (!userService.isEmailAvailable(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already in use");
            return "users/register";
        }

        if (result.hasErrors()) {
            return "users/register";
        }

        try {
            // Log original password length (don't log the actual password!)
            log.debug("Password length before encoding: {}", user.getPassword().length());

            // Encode password and set default role
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.addRole(Role.USER);

            log.debug("Attempting to register new user with username: {}", user.getUsername());
            User createdUser = userService.createUser(user);

            log.info("Successfully registered new user: {}", createdUser.getUsername());
            return "redirect:/login?registered";
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            result.rejectValue("username", "error.user", "Registration failed: " + e.getMessage());
            return "users/register";
        }
    }

    @Operation(summary = "Show the user profile")
    @GetMapping("/profile")
    public String showProfile(Model model) {
        // To be implemented after adding authentication
        return "users/profile";
    }
}