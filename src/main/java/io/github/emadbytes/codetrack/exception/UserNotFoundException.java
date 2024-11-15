// src\main\java\io\github\emadbytes\codetrack\exception\UserNotFoundException.java
package io.github.emadbytes.codetrack.exception;

/**
 * Exception thrown when attempting to operate on a user that doesn't exist.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with the specified user ID.
     * 
     * @param id the ID of the user that was not found
     */
    public UserNotFoundException(Long id) {
        super(String.format("User not found with ID: %d", id));
    }

    /**
     * Constructs a new exception with the specified username.
     * 
     * @param username the username that was not found
     */
    public UserNotFoundException(String username) {
        super(String.format("User not found with username: %s", username));
    }
}