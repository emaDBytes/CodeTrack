// src\test\java\io\github\emadbytes\codetrack\security\CodetrackUserDetailsTest.java
package io.github.emadbytes.codetrack.security;

import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CodetrackUserDetails.
 */
class CodetrackUserDetailsTest {

    private User user;
    private CodetrackUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setActive(true);
        user.addRole(Role.USER);

        userDetails = new CodetrackUserDetails(user);
    }

    @Test
    void whenGetAuthorities_thenCorrectRoles() {
        // given user with ROLE_USER from setup

        // when
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities)
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void whenUserIsActive_thenEnabled() {
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void whenUserIsInactive_thenDisabled() {
        // given
        user.setActive(false);

        // then
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void whenGetCredentials_thenCorrectValues() {
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
    }
}
