package com.example.codetrack.repository;

import com.example.codetrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity.
 * Extends JpaRepository to inherit basic CRUD operations and more.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     * 
     * @param username the username to search for
     * @return the user if found, or null if not found
     */
    User findByUsername(String username);

    /**
     * Find a user by email address.
     * 
     * @param email the email to search for
     * @return the user if found, or null if not found
     */
    User findByEmail(String email);

    /**
     * Check if a username already exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}