package com.wildguard.conservation.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Security settings of my service.
// 1. no session and no login page here: every request must bring the JWT token
// 2. only ADMIN, FOREST_OFFICER and RESCUE_OFFICER can use the conservation API
// 3. CORS: the pages are opened from the platform app (port 8081) but they call my service
//    (port 8084). The browser blocks that unless my service says the other address is allowed.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // must be the same secret as JwtService in the platform app
    @Value("${wildguard.jwt-secret}")
    private String jwtSecret;

    // the address of the platform app, where the pages come from
    @Value("${wildguard.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // the browser sends an OPTIONS request first (CORS check), it has no token
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/conservation/health", "/error").permitAll()
                        .anyRequest().hasAnyRole("ADMIN", "FOREST_OFFICER", "RESCUE_OFFICER"))
                .exceptionHandling(errors -> errors
                        // no token or bad token -> 401 with a small JSON message
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"status\":401,\"message\":\"Please log in again.\"}");
                        })
                        // logged in but the role is not allowed -> 403
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":403,\"message\":\"Your role is not allowed to use this feature.\"}");
                        }))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(new JwtAuthFilter(jwtSecret), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split("\\s*,\\s*")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // My service has no users of its own (the platform app has them).
    // This empty bean only stops Spring Boot from creating a default user with a random password.
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
