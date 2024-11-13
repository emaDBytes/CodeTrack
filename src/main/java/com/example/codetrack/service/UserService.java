package com.example.codetrack.service;

import com.example.codetrack.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing User entities.
 * Defines business operations available for User management.
 */
public interface UserService {

    /**
     * Create a new user.
     * 
     * @param user the user to create
     * @return the created user
     * @throws RuntimeException if username or email already exists
     */
    User createUser(User user);

    /**
     * Find a user by their ID.
     * 
     * @param id the user ID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserById(Long id);

    /**
     * Find a user by their username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserByUsername(String username);

    /**
     * Find a user by their email.
     * 
     * @param email the email to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Get all users in the system.
     * 
     * @return list of all users
     */
    List<User> getAllUsers();

    /**
     * Update an existing user's information.
     * 
     * @param user the user with updated information
     * @return the updated user
     * @throws RuntimeException if user doesn't exist
     */
    User updateUser(User user);

    /**
     * Delete a user by their ID.
     * 
     * @param id the ID of the user to delete
     * @throws RuntimeException if user doesn't exist
     */
    void deleteUser(Long id);

    /**
     * Check if a username is available.
     * 
     * @param username the username to check
     * @return true if username is available, false otherwise
     */
    boolean isUsernameAvailable(String username);

    /**
     * Check if an email is available.
     * 
     * @param email the email to check
     * @return true if email is available, false otherwise
     */
    boolean isEmailAvailable(String email);
}