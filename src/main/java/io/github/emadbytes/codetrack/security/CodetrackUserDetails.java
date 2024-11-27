// src/main/java/io/github/emadbytes/codetrack/security/CodetrackUserDetails.java
package io.github.emadbytes.codetrack.security;

import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.model.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CodetrackUserDetails implements UserDetails, OAuth2User {
    private final User user;
    private Map<String, Object> attributes;

    // Constructor for regular authentication
    public CodetrackUserDetails(User user) {
        this.user = user;
    }

    // Constructor for OAuth2 authentication
    public CodetrackUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return user.getUsername();
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

    public User getUser() {
        return user;
    }

    public Set<Role> getRoles() {
        return user.getRoles();
    }
}