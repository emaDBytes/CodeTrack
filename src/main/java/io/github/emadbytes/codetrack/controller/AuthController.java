// src\main\java\io\github\emadbytes\codetrack\controller\AuthController.java
package io.github.emadbytes.codetrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller for handling authentication-related pages.
 */
@Controller
public class AuthController {

    @Operation(summary = "Show the login page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login page successfully retrieved", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @Operation(summary = "Redirect the root URL to the dashboard or login page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirected to the appropriate page", content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}