// src\main\java\io\github\emadbytes\codetrack\exception\DuplicateUserException.java
package io.github.emadbytes.codetrack.exception;

/**
 * Exception thrown when attempting to create a user with existing username or
 * email.
 */
public class DuplicateUserException extends RuntimeException {

    /**
     * Constructs a new exception with the specified field and value.
     * 
     * @param field the field that caused the duplicate (username or email)
     * @param value the duplicate value
     */
    public DuplicateUserException(String field, String value) {
        super(String.format("%s already exists: %s", field, value));
    }
}