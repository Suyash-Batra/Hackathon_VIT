
package com.example.Hackathon.controller;

import com.example.Hackathon.model.User;
import com.example.Hackathon.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMetricsController {

    private final UserRepository userRepository;

    public AdminMetricsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {

        List<User> users = userRepository.findAll();

        Map<String, Object> metrics = new LinkedHashMap<>();

        metrics.put("totalUsers", users.size());

        metrics.put("activeUsers", users.stream()
                .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                .count());

        metrics.put("pendingUsers", users.stream()
                .filter(user -> "PENDING".equalsIgnoreCase(user.getStatus()))
                .count());

        metrics.put("inactiveUsers", users.stream()
                .filter(user -> "INACTIVE".equalsIgnoreCase(user.getStatus()))
                .count());

        metrics.put("forestOfficers", users.stream()
                .filter(user -> "FOREST_OFFICER".equalsIgnoreCase(user.getRole()))
                .count());

        metrics.put("rescueOfficers", users.stream()
                .filter(user -> "RESCUE_OFFICER".equalsIgnoreCase(user.getRole()))
                .count());

        return metrics;
    }
}
