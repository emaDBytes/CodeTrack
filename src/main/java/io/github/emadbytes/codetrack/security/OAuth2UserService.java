// src/main/java/io/github/emadbytes/codetrack/security/OAuth2UserService.java
package io.github.emadbytes.codetrack.security;

import io.github.emadbytes.codetrack.model.Role;
import io.github.emadbytes.codetrack.model.User;
import io.github.emadbytes.codetrack.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public OAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("OAuth2 authentication attempt started");
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            log.debug("Google user info received: {}", oauth2User.getAttributes());

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            log.debug("Extracted user info - email: {}, name: {}", email, name);

            User user = userService.getUserByEmail(email)
                    .orElseGet(() -> {
                        log.debug("Creating new user for email: {}", email);
                        return createNewUser(email, name);
                    });

            CodetrackUserDetails userDetails = new CodetrackUserDetails(user, oauth2User.getAttributes());
            log.debug("Successfully created UserDetails for: {}", email);
            return userDetails;

        } catch (Exception e) {
            log.error("Error in OAuth2 authentication process", e);
            throw e;
        }
    }

    private User createNewUser(String email, String name) {
        log.debug("Creating new user from Google account: {}", email);

        User user = new User();
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf('@'))); // Use email prefix as username
        user.setIsOAuth2User(true);
        user.setPassword(""); // No password for OAuth users
        user.setActive(true);
        user.addRole(Role.USER);

        User savedUser = userService.createUser(user);
        log.debug("Created new user with ID: {}", savedUser.getId());
        return savedUser;
    }
}