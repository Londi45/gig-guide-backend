package com.Gig.Guide.GigGuide.Config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security config for @WebMvcTest slices.
 *
 * Replaces the production SecurityConfig (which wires JwtAuthFilter and
 * MyAppUserService — beans not available in the web slice). This config:
 *   - Enables @PreAuthorize via @EnableMethodSecurity
 *   - Mirrors the permitAll rules from production for GET /api/clubs/**
 *   - Has no JWT filter — @WithMockUser populates the security context directly
 *
 * Annotated @TestConfiguration so it is NOT picked up by component scanning
 * in full @SpringBootTest contexts; it is only activated when explicitly
 * imported with @Import.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/clubs/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getOutputStream().write("{\"status\":401}".getBytes());
                })
            );
        return http.build();
    }
}
