// src\main\java\io\github\emadbytes\codetrack\dto\NewSessionRequest.java
package io.github.emadbytes.codetrack.dto;

import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * DTO for creating a new coding session.
 * Contains validated fields required for starting a new session.
 */
@Data
public class NewSessionRequest {

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    private String projectName;
}