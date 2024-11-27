// src\main\java\io\github\emadbytes\codetrack\config\SecurityConfig.java
package io.github.emadbytes.codetrack.config;

import io.github.emadbytes.codetrack.security.OAuth2UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

        private final OAuth2UserService oAuth2UserService;

        public SecurityConfig(OAuth2UserService oAuth2UserService) {
                this.oAuth2UserService = oAuth2UserService;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/oauth2/**").permitAll()
                                                .requestMatchers("/", "/home", "/users/register", "/h2-console/**")
                                                .permitAll()
                                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/dashboard")
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(oAuth2UserService))
                                                .successHandler((request, response, authentication) -> {
                                                        log.info("OAuth2 login successful for user: {}",
                                                                        authentication.getName());
                                                        response.sendRedirect("/dashboard");
                                                })
                                                .failureHandler((request, response, exception) -> {
                                                        log.error("OAuth2 login failed", exception);
                                                        response.sendRedirect("/login?error=oauth2");
                                                }))
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}