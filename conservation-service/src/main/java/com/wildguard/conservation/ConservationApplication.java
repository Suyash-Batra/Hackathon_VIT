package com.wildguard.conservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Main class of my service (conservation-service). It runs on port 8084.
// It handles rescue cases, vehicles/equipment, programmes and the incident report.
// We also scan com.wildguard.common because the shared security and error classes are there.
@SpringBootApplication(scanBasePackages = { "com.wildguard.conservation", "com.wildguard.common" },
        exclude = UserDetailsServiceAutoConfiguration.class)
public class ConservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConservationApplication.class, args);
    }
}
