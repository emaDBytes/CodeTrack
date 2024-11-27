// src/main/java/io/github/emadbytes/codetrack/model/User.java
package io.github.emadbytes.codetrack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a user in the CodeTrack application.
 * This class maps to the 'users' table in the database.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;

    private String password;

    private boolean active = true;

    private Boolean isOAuth2User = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    /**
     * Handles entity lifecycle events before persisting or updating.
     * - Sets timestamps
     * - Validates password requirements
     */
    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        // Handle timestamps
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();

        // Validate password
        if (!isOAuth2User && (password == null || password.trim().isEmpty())) {
            throw new IllegalStateException("Password is required for non-OAuth2 users");
        }
    }

    /**
     * Adds a role to the user.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        roles.add(role);
    }

    /**
     * Removes a role from the user.
     *
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        roles.remove(role);
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}