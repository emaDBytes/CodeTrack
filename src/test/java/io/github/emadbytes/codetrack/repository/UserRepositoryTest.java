// src\test\java\io\github\emadbytes\codetrack\repository\UserRepositoryTest.java
package io.github.emadbytes.codetrack.repository;

import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 * Uses Spring's @DataJpaTest to set up an in-memory database and configure
 * appropriate Spring components for testing JPA repositories.
 *
 * @see DataJpaTest
 * @see UserRepository
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    /**
     * Set up method that runs before each test.
     * Creates a standard test user with predefined values.
     * This user can be modified for specific test cases as needed.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setActive(true);
    }

    /**
     * Test finding a user by username.
     * Verifies that:
     * 1. The user can be successfully persisted
     * 2. The user can be retrieved by username
     * 3. The retrieved user matches the persisted user
     */
    @Test
    void whenFindByUsername_thenReturnUser() {
        // given
        User persistedUser = entityManager.persistFlushFind(testUser);

        // when
        Optional<User> found = userRepository.findByUsername(testUser.getUsername());

        // then
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getUsername()).isEqualTo(persistedUser.getUsername());
                    assertThat(user.getEmail()).isEqualTo(persistedUser.getEmail());
                });
    }

    /**
     * Test finding a user by email.
     * Verifies that:
     * 1. The user can be successfully persisted
     * 2. The user can be retrieved by email
     * 3. The retrieved user matches the persisted user
     */
    @Test
    void whenFindByEmail_thenReturnUser() {
        // given
        User persistedUser = entityManager.persistFlushFind(testUser);

        // when
        Optional<User> found = userRepository.findByEmail(testUser.getEmail());

        // then
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getUsername()).isEqualTo(persistedUser.getUsername());
                    assertThat(user.getEmail()).isEqualTo(persistedUser.getEmail());
                });
    }

    /**
     * Test finding a user by non-existent username.
     * Verifies that searching for a non-existent username returns an empty
     * Optional.
     */
    @Test
    void whenFindByNonExistentUsername_thenReturnEmpty() {
        // when
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    /**
     * Test finding a user by non-existent email.
     * Verifies that searching for a non-existent email returns an empty Optional.
     */
    @Test
    void whenFindByNonExistentEmail_thenReturnEmpty() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    /**
     * Test checking if a username exists.
     * Verifies that:
     * 1. The user can be successfully persisted
     * 2. The exists check returns true for an existing username
     */
    @Test
    void whenExistsByUsername_thenReturnTrue() {
        // given
        entityManager.persistAndFlush(testUser);

        // when
        boolean exists = userRepository.existsByUsername(testUser.getUsername());

        // then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if an email exists.
     * Verifies that:
     * 1. The user can be successfully persisted
     * 2. The exists check returns true for an existing email
     */
    @Test
    void whenExistsByEmail_thenReturnTrue() {
        // given
        entityManager.persistAndFlush(testUser);

        // when
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        // then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a non-existent username exists.
     * Verifies that the exists check returns false for a non-existent username.
     */
    @Test
    void whenExistsByNonExistentUsername_thenReturnFalse() {
        // when
        boolean exists = userRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    /**
     * Test checking if a non-existent email exists.
     * Verifies that the exists check returns false for a non-existent email.
     */
    @Test
    void whenExistsByNonExistentEmail_thenReturnFalse() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    /**
     * Test persisting and retrieving user roles.
     */
    @Test
    void whenSAveUserWithRoles_thenRolesPersisted() {
        // given
        User user = new User();
        user.setUsername("roleuser");
        user.setEmail("role@example.com");
        user.setPassword("password123");
        user.setActive(true);
        user.addRole(Role.USER);
        user.addRole(Role.ADMIN);

        // when
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear(); // Clear persistence context to force reload from database

        User retrievedUser = userRepository.findById(savedUser.getId()).get();

        // then
        Assertions.assertThat(retrievedUser.getRoles())
                .hasSize(2)
                .containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }
}