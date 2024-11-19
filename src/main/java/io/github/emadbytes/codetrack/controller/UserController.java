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

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

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

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // To be implemented after adding authentication
        return "users/profile";
    }
}