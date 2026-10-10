
package com.example.Hackathon.controller;

import com.example.Hackathon.dto.RoleUpdateRequest;
import com.example.Hackathon.model.User;
import com.example.Hackathon.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {

        return userRepository.findAll().stream().map(user -> {
            Map<String, Object> data = new LinkedHashMap<>();

            data.put("id", user.getId());
            data.put("name", user.getName());
            data.put("email", user.getEmail());
            data.put("role", user.getRole());
            data.put("status", user.getStatus());
            data.put("createdAt", user.getCreatedAt());

            return data;
        }).toList();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {

        List<String> allowedRoles = List.of(
                "USER",
                "ADMIN",
                "FOREST_OFFICER",
                "RESCUE_OFFICER"
        );

        if (request.getRole() == null
                || !allowedRoles.contains(request.getRole())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid role"));
        }

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setRole(request.getRole());
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Role updated successfully")
        );
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        if (status == null
                || !List.of("ACTIVE", "PENDING", "INACTIVE")
                .contains(status)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid status"));
        }

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus(status);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "User status updated successfully")
        );
    }
}
