package com.wildguard.conservation.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// This filter runs before every request.
// The user logs in on the platform app and gets a JWT token. The page sends that token to my
// service in the header "Authorization: Bearer <token>". Here I check the token with the SAME
// secret key the platform app used to sign it. If it is fine I read the email and the role from it.
public class JwtAuthFilter extends OncePerRequestFilter {

    private SecretKey key;

    public JwtAuthFilter(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                // parseSignedClaims throws an exception if the token is fake or expired
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

                String email = claims.getSubject();
                Role role = toRole(claims.get("role", String.class));

                Caller caller = new Caller(email, role, token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        caller, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                // bad token = not logged in. Spring Security will answer 401.
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    // "ROLE_ADMIN" or "admin" both become Role.ADMIN. Anything we dont know becomes USER.
    private Role toRole(String text) {
        if (text == null) {
            return Role.USER;
        }
        String name = text.trim().toUpperCase();
        if (name.startsWith("ROLE_")) {
            name = name.substring(5);
        }
        try {
            return Role.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return Role.USER;
        }
    }
}
