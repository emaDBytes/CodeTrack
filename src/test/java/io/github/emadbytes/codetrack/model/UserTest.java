// src\test\java\io\github\emadbytes\codetrack\model\UserTest.java
package io.github.emadbytes.codetrack.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for User entity.
 * Tests validation and basic functionality of the User class.
 */
public class UserTest {

    private Validator validator;
    private User user;

    /**
     * Set up method that runs before each test.
     * Initializes the validator and creates a test user.
     */
    @BeforeEach
    void setUp() {
        // Create validator instance
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create a valid user for testing
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setActive(true);
    }

    /**
     * Test valid user creation with all required fields.
     */
    @Test
    void testValidUser() {
        var violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Validation should pass for a valid user");
    }

    /**
     * Test validation when username is blank.
     */
    @Test
    void testBlankUsername() {
        user.setUsername("");
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Should have validation violation for blank username");
        assertEquals(1, violations.size(), "Should have exactly one validation violation");
        assertEquals("Username is required",
                violations.iterator().next().getMessage(),
                "Should have correct error message");
    }

    /**
     * Test validation when email is invalid.
     */
    @Test
    void testInvalidEmail() {
        user.setEmail("invalid-email");
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Should have validation violation for invalid email");
        assertEquals(1, violations.size(), "Should have exactly one validation violation");
    }

    /**
     * Test automatic timestamp setting on creation.
     */
    @Test
    void testTimestampGeneration() {
        assertNull(user.getCreatedAt(), "CreatedAt should be null before persist");
        user.onCreate();
        assertNotNull(user.getCreatedAt(), "CreatedAt should be set after onCreate");
        assertNotNull(user.getUpdatedAt(), "UpdatedAt should be set after onCreate");
    }

    /**
     * Test update timestamp modification.
     */
    @Test
    void testUpdateTimestamp() {
        user.onCreate();
        LocalDateTime originalUpdate = user.getUpdatedAt();

        // Wait a small amount of time to ensure different timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        user.onUpdate();
        assertNotEquals(originalUpdate, user.getUpdatedAt(),
                "UpdatedAt should change after onUpdate");
    }

    /**
     * Test Lombok-generated equals method.
     */
    @Test
    void testEquals() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("user1");

        assertEquals(user1, user2, "Users with same ID should be equal");
    }

    /**
     * Test adding and checking roles.
     */
    @Test
    void testRoleManagement() {
        User user = new User();

        // Test adding roles
        user.addRole(Role.USER);
        Assertions.assertThat(user.getRoles()).contains(Role.USER);

        // Test hasRole method
        assertThat(user.hasRole(Role.USER)).isTrue();
        assertThat(user.hasRole(Role.ADMIN)).isFalse();

        // Test adding multiple roles
        user.addRole(Role.ADMIN);
        Assertions.assertThat(user.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);

        // Test removing roles
        user.removeRole(Role.USER);
        Assertions.assertThat(user.getRoles()).containsExactly(Role.ADMIN);
    }

    /**
     * Test role enumeration functionality.
     */
    @Test
    void testRoleEnumeration() {
        assertThat(Role.USER.getFullRoleName()).isEqualTo("ROLE_USER");
        assertThat(Role.ADMIN.getFullRoleName()).isEqualTo("ROLE_ADMIN");
    }
}