// src\main\java\io\github\emadbytes\codetrack\exception\InvalidSessionOperationException.java
package io.github.emadbytes.codetrack.exception;

/**
 * Exception thrown when an invalid operation is attempted on a coding session.
 * For example, trying to end an already completed session.
 */
public class InvalidSessionOperationException extends RuntimeException {

    public InvalidSessionOperationException(String message) {
        super(message);
    }
}