package com.wildguard.conservation.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// The person who is calling my API right now.
// JwtAuthFilter creates this object from the token, and the services read it with Caller.current().
public class Caller {

    private String email;
    private Role role;
    private String token;

    public Caller(String email, Role role, String token) {
        this.email = email;
        this.role = role;
        this.token = token;
    }

    // gives the caller of the current request
    public static Caller current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Caller) {
            return (Caller) authentication.getPrincipal();
        }
        // should not happen because every /api url needs a token
        return new Caller("unknown", Role.USER, null);
    }

    // true if the caller has one of the given roles
    public boolean hasRole(Role... roles) {
        for (Role r : roles) {
            if (r == role) {
                return true;
            }
        }
        return false;
    }

    // the name we save in history rows (the token only has the email, so we use that)
    public String displayName() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    // the token is kept so we can pass it on when we call another service
    public String getToken() {
        return token;
    }
}
