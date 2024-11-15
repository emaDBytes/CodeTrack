// src/test/java/io/github/emadbytes/codetrack/security/CodetrackUserDetailsServiceTest.java
package io.github.emadbytes.codetrack.security;

import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for CodetrackUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
class CodetrackUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CodetrackUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CodetrackUserDetailsService(userRepository);
    }

    @Test
    void whenLoadByUsername_thenReturnUserDetails() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        given(userRepository.findByUsername("testuser"))
                .willReturn(Optional.of(user));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // then
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
    }

    @Test
    void whenLoadByNonExistentUsername_thenThrowException() {
        // given
        given(userRepository.findByUsername("nonexistent"))
                .willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}