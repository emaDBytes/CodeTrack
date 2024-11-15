// src/main/java/io/github/emadbytes/codetrack/security/CodetrackUserDetailsService.java
package io.github.emadbytes.codetrack.security;

import io.github.emadbytes.codetrack.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user-specific data for authentication.
 */
@Service
@Slf4j
public class CodetrackUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CodetrackUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByUsername(username)
                .map(CodetrackUserDetails::new)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }
}