package com.example.codetrack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity class representing a user in the CodeTrack application.
 * This class maps to the 'users' table in the database.
 */
@Entity
@Table(name = "users")
@Data               // Lombok: Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor  // Lombok: Generates a no-args constructor required by JPA
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
public class User {

    /**
     * Unique identifier for the user.
     * Auto-generated when a new user is persisted.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's username. Must be unique and cannot be blank.
     */
    @NotBlank(message = "Username is required")
    @Column(unique = true)
    private String username;

    /**
     * User's email address. Must be unique and properly formatted.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;

    /**
     * User's password. Will be encrypted before storage.
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Flag indicating if the user account is active.
     * Defaults to true for new accounts.
     */
    private boolean active = true;

    /**
     * Timestamp of when the user account was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update to the user account.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Automatically sets creation and update timestamps when
     * a new user is persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically updates the update timestamp when
     * a user entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}