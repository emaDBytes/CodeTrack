// src\test\java\io\github\emadbytes\codetrack\controller\UserControllerTest.java
package io.github.emadbytes.codetrack.controller;

import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController.
 * Uses MockMvc to test HTTP requests and responses.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userService, passwordEncoder);
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
    }

    @Test
    void whenGetRegister_thenReturnRegistrationForm() throws Exception {
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void whenPostValidUser_thenRedirectToLogin() throws Exception {
        // Given
        given(userService.isUsernameAvailable(anyString())).willReturn(true);
        given(userService.isEmailAvailable(anyString())).willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userService.createUser(any(User.class))).willReturn(testUser);

        // When/Then
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", testUser.getUsername())
                .param("email", testUser.getEmail())
                .param("password", testUser.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void whenUsernameAlreadyExists_thenReturnRegistrationForm() throws Exception {
        // Given
        given(userService.isUsernameAvailable(anyString())).willReturn(false);

        // When/Then
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", testUser.getUsername())
                .param("email", testUser.getEmail())
                .param("password", testUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));
    }

    @Test
    void whenEmailAlreadyExists_thenReturnRegistrationForm() throws Exception {
        // Given
        given(userService.isUsernameAvailable(anyString())).willReturn(true);
        given(userService.isEmailAvailable(anyString())).willReturn(false);

        // When/Then
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", testUser.getUsername())
                .param("email", testUser.getEmail())
                .param("password", testUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    @Test
    void whenPostInvalidUser_thenReturnRegistrationForm() throws Exception {
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "") // Invalid: empty username
                .param("email", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"));
    }

    @Test
    void whenGetProfile_thenReturnProfilePage() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/profile"));
    }
}