package com.example.Hackathon.controller;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformHealthController {

    private final JdbcTemplate jdbcTemplate;

    public PlatformHealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            return ResponseEntity.ok(Map.of(
                    "service", "platform-service",
                    "status", "UP",
                    "database", "CONNECTED"
            ));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of(
                            "service", "platform-service",
                            "status", "DOWN",
                            "database", "DISCONNECTED"
                    )
            );
        }
    }
}