// src\test\java\io\github\emadbytes\codetrack\service\UserServiceTest.java
package io.github.emadbytes.codetrack.service;

import io.github.emadbytes.codetrack.exception.DuplicateUserException;
import io.github.emadbytes.codetrack.exception.UserNotFoundException;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.UserRepository;
import io.github.emadbytes.codetrack.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for UserService implementation.
 * Uses Mockito framework to mock dependencies and verify behavior.
 *
 * @see UserService
 * @see UserServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private User testUser;

    /**
     * Set up method that runs before each test.
     * Initializes the UserService with mocked repository and creates a test user.
     */
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);

        // Create test user with standard test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setActive(true);
    }

    /**
     * Test successful user creation when username and email are available.
     */
    @Test
    void whenCreateUser_thenSucceed() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(testUser.getEmail())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User created = userService.createUser(testUser);

        // then
        assertThat(created)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                    assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
                });
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test user creation fails when username already exists.
     */
    @Test
    void whenCreateUserWithExistingUsername_thenThrowException() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(true);

        // when/then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test user creation fails when email already exists.
     */
    @Test
    void whenCreateUserWithExistingEmail_thenThrowException() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(testUser.getEmail())).willReturn(true);

        // when/then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test successful retrieval of user by ID.
     */
    @Test
    void whenGetUserById_thenReturnUser() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.getUserById(1L);

        // then
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(testUser.getId());
                    assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                });
    }

    /**
     * Test successful retrieval of user by username.
     */
    @Test
    void whenGetUserByUsername_thenReturnUser() {
        // given
        given(userRepository.findByUsername(testUser.getUsername()))
                .willReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.getUserByUsername(testUser.getUsername());

        // then
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                    assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
                });
    }

    /**
     * Test successful retrieval of user by email.
     */
    @Test
    void whenGetUserByEmail_thenReturnUser() {
        // given
        given(userRepository.findByEmail(testUser.getEmail()))
                .willReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.getUserByEmail(testUser.getEmail());

        // then
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
                    assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                });
    }

    /**
     * Test retrieval of all users.
     */
    @Test
    void whenGetAllUsers_thenReturnList() {
        // given
        User secondUser = new User();
        secondUser.setUsername("testuser2");
        List<User> users = Arrays.asList(testUser, secondUser);
        given(userRepository.findAll()).willReturn(users);

        // when
        List<User> allUsers = userService.getAllUsers();

        // then
        assertThat(allUsers)
                .hasSize(2)
                .contains(testUser, secondUser);
    }

    /**
     * Test successful user update.
     */
    @Test
    void whenUpdateUser_thenSucceed() {
        // given
        given(userRepository.existsById(testUser.getId())).willReturn(true);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User updated = userService.updateUser(testUser);

        // then
        assertThat(updated)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                    assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
                });
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test update fails when user doesn't exist.
     */
    @Test
    void whenUpdateNonExistingUser_thenThrowException() {
        // given
        given(userRepository.existsById(testUser.getId())).willReturn(false);

        // when/then
        assertThatThrownBy(() -> userService.updateUser(testUser))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test successful user deletion.
     */
    @Test
    void whenDeleteUser_thenSucceed() {
        // given
        given(userRepository.existsById(1L)).willReturn(true);

        // when
        userService.deleteUser(1L);

        // then
        verify(userRepository).deleteById(1L);
    }

    /**
     * Test deletion fails when user doesn't exist.
     */
    @Test
    void whenDeleteNonExistingUser_thenThrowException() {
        // given
        given(userRepository.existsById(1L)).willReturn(false);

        // when/then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).deleteById(any());
    }

    /**
     * Test username availability check.
     */
    @Test
    void whenCheckUsernameAvailability_thenReturnTrue() {
        // given
        given(userRepository.existsByUsername("newuser")).willReturn(false);

        // when
        boolean isAvailable = userService.isUsernameAvailable("newuser");

        // then
        assertThat(isAvailable).isTrue();
    }

    /**
     * Test email availability check.
     */
    @Test
    void whenCheckEmailAvailability_thenReturnTrue() {
        // given
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);

        // when
        boolean isAvailable = userService.isEmailAvailable("new@example.com");

        // then
        assertThat(isAvailable).isTrue();
    }
}