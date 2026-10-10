package com.wildguard.conservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main class of my service (conservation-service). It runs on port 8084.
// It handles rescue cases, vehicles/equipment and conservation programmes.
// The login is done by the platform app (port 8081). My service only checks the JWT token it gives.
@SpringBootApplication
public class ConservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConservationApplication.class, args);
    }
}
