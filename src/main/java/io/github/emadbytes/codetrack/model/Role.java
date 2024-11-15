// src\main\java\io\github\emadbytes\codetrack\model\Role.java
package io.github.emadbytes.codetrack.model;

/**
 * Enumeration of user roles in the system.
 * Defines the possible authorization levels for users.
 */
public enum Role {

    /**
     * Regular user role with basic permissions
     */
    USER,

    /**
     * Administrator role with full system access
     */
    ADMIN;

    /**
     * Returns the role name with the standard "ROLE_" prefix required by Spring
     * Security.
     *
     * @return the full role name (e.g., "ROLE_USER")
     */
    public String getFullRoleName() {
        return "ROLE_" + this.name();
    }
}
