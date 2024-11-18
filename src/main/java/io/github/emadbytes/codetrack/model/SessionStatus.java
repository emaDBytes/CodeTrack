// src\main\java\io\github\emadbytes\codetrack\model\SessionStatus.java
package io.github.emadbytes.codetrack.model;

/**
 * Enumeration representing the possible states of a coding session.
 * Used to track the lifecycle of coding sessions in the system.
 */
public enum SessionStatus {
    /**
     * Indicates an active coding session that is currently running.
     * This is the initial status when a session is created.
     */
    IN_PROGRESS,

    /**
     * Indicates a coding session that has been properly finished.
     * Duration is calculated when a session reaches this status.
     */
    COMPLETED,

    /**
     * Indicates a coding session that was terminated before completion.
     * Used when a session is abandoned or stopped prematurely.
     */
    CANCELLED
}