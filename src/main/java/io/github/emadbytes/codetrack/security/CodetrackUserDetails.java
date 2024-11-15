// src/main/java/io/github/emadbytes/codetrack/security/CodetrackUserDetails.java
package io.github.emadbytes.codetrack.security;

import io.github.emadbytes.codetrack.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementation of Spring Security's UserDetails interface.
 * Adapts our custom User entity to work with Spring Security.
 */
public class CodetrackUserDetails implements UserDetails {

    private final User user;

    public CodetrackUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getFullRoleName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * Gets the underlying user entity.
     *
     * @return the user entity
     */
    public User getUser() {
        return user;
    }
}