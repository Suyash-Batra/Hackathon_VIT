package com.wildguard.conservation.util;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

import com.wildguard.conservation.util.ErrorHandler.AppException;

// Login is done by the platform app (Suyash's part). After login the page gets a token and
// sends it to my service in the header  "Authorization: Bearer <token>".
// Here I read that token with the same secret key and the same library the platform uses,
// so I know the email and the role of the user. Every controller method calls check() first.
@Component
public class LoginCheck {

    // must be the same text as the secret in the platform's JwtService
    @Value("${wildguard.jwt-secret}")
    private String secret;

    // roles: the roles allowed for this action. if none are given, any staff role is fine.
    public LoginUser check(HttpServletRequest request, String... roles) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new AppException(401, "Please log in again.");
        }
        String token = header.substring(7).trim();

        LoginUser user;
        try {
            // this throws an exception if the token is changed by someone or is too old
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build().parseSignedClaims(token).getPayload();
            String role = String.valueOf(claims.get("role")).toUpperCase().replace("ROLE_", "");
            user = new LoginUser(claims.getSubject(), role, token);
        } catch (Exception ex) {
            throw new AppException(401, "Please log in again.");
        }

        // a normal USER cannot use the conservation part at all
        if (!user.is("ADMIN", "FOREST_OFFICER", "RESCUE_OFFICER")) {
            throw new AppException(403, "Your role is not allowed to use this feature.");
        }
        if (roles.length > 0 && !user.is(roles)) {
            throw new AppException(403, "Your role is not allowed to perform this action.");
        }
        return user;
    }

    // the person who is calling my API. made by LoginCheck from the login token
    public static class LoginUser {

        public String email;
        public String role; // ADMIN, FOREST_OFFICER, RESCUE_OFFICER or USER
        public String token;

        public LoginUser(String email, String role, String token) {
            this.email = email;
            this.role = role;
            this.token = token;
        }

        // true if the user has one of the given roles
        public boolean is(String... roles) {
            for (String r : roles) {
                if (r.equals(role)) {
                    return true;
                }
            }
            return false;
        }
    }
}
