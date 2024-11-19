// src\main\java\io\github\emadbytes\codetrack\service\impl\UserServiceImpl.java
package io.github.emadbytes.codetrack.service.impl;

import io.github.emadbytes.codetrack.exception.DuplicateUserException;
import io.github.emadbytes.codetrack.exception.UserNotFoundException;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.UserRepository;
import io.github.emadbytes.codetrack.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService interface.
 * Provides business logic for User management.
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        log.debug("Attempting to create user with username: {}", user.getUsername());

        if (!isUsernameAvailable(user.getUsername())) {
            log.error("Username already exists: {}", user.getUsername());
            throw new DuplicateUserException("Username", user.getUsername());
        }
        if (!isEmailAvailable(user.getEmail())) {
            log.error("Email already exists: {}", user.getEmail());
            throw new DuplicateUserException("Email", user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        log.debug("Attempting to update user with ID: {}", user.getId());

        if (!userRepository.existsById(user.getId())) {
            log.error("User not found with ID: {}", user.getId());
            throw new UserNotFoundException(user.getId());
        }

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Attempting to delete user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.error("User not found with ID: {}", id);
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}