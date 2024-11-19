// src\main\java\io\github\emadbytes\codetrack\controller\AuthController.java
package io.github.emadbytes.codetrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling authentication-related pages.
 */
@Controller
public class AuthController {

    /**
     * Shows the login page.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    /**
     * Redirects root URL to dashboard or login page depending on authentication
     * status.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}