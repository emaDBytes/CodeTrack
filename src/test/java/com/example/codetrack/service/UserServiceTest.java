package com.example.codetrack.service;

import com.example.codetrack.model.User;
import com.example.codetrack.repository.UserRepository;
import com.example.codetrack.service.impl.UserServiceImpl;
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
import static org.mockito.Mockito.verify;

/**
 * Test class for UserService implementation.
 * Uses Mockito to mock the UserRepository.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setActive(true);
    }

    @Test
    void whenCreateUser_thenSucceed() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(testUser.getEmail())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User created = userService.createUser(testUser);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getUsername()).isEqualTo(testUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenCreateUserWithExistingUsername_thenThrowException() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(true);

        // when/then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void whenGetUserById_thenReturnUser() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.getUserById(1L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void whenGetUserByUsername_thenReturnUser() {
        // given
        given(userRepository.findByUsername(testUser.getUsername()))
                .willReturn(testUser);

        // when
        Optional<User> found = userService.getUserByUsername(testUser.getUsername());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
    }

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
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).contains(testUser, secondUser);
    }

    @Test
    void whenUpdateUser_thenSucceed() {
        // given
        given(userRepository.existsById(testUser.getId())).willReturn(true);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User updated = userService.updateUser(testUser);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getUsername()).isEqualTo(testUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenUpdateNonExistingUser_thenThrowException() {
        // given
        given(userRepository.existsById(testUser.getId())).willReturn(false);

        // when/then
        assertThatThrownBy(() -> userService.updateUser(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void whenDeleteUser_thenSucceed() {
        // given
        given(userRepository.existsById(1L)).willReturn(true);

        // when
        userService.deleteUser(1L);

        // then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void whenCheckUsernameAvailability_thenReturnTrue() {
        // given
        given(userRepository.existsByUsername("newuser")).willReturn(false);

        // when
        boolean isAvailable = userService.isUsernameAvailable("newuser");

        // then
        assertThat(isAvailable).isTrue();
    }
}