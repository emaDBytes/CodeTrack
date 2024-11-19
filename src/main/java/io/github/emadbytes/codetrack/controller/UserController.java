// src\main\java\io\github\emadbytes\codetrack\controller\UserController.java
package io.github.emadbytes.codetrack.controller;

import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.service.UserService;

import lombok.extern.slf4j.Slf4j;

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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "users/register";
        }

        try {
            userService.createUser(user);
            return "redirect:/login?registered";
        } catch (Exception e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "users/register";
        }

    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // To be implemented after adding authentication
        return "users/profile";
    }

}
