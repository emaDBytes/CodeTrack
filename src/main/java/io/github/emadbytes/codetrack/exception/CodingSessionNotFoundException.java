// src\main\java\io\github\emadbytes\codetrack\exception\CodingSessionNotFoundException.java
package io.github.emadbytes.codetrack.exception;

/**
 * Exception thrown when a requested coding session cannot be found.
 */
public class CodingSessionNotFoundException extends RuntimeException {

    public CodingSessionNotFoundException(Long sessionId) {
        super("Could not find coding session with id: " + sessionId);
    }
}