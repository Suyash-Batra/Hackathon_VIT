package com.example.Hackathon.controller;

import com.example.Hackathon.service.RiskRadarService;
import com.example.Hackathon.service.RiskRadarService.RiskResult;
import com.example.Hackathon.service.RiskRadarService.ZoneRiskInput;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/risk-radar")
@PreAuthorize("hasAnyRole('ADMIN', 'FOREST_OFFICER', 'RESCUE_OFFICER')")
public class RiskRadarController {

    private final RiskRadarService riskRadarService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wildguard.services.wildlife-url:http://localhost:8082}")
    private String wildlifeUrl;

    @Value("${wildguard.services.operations-url:http://localhost:8083}")
    private String operationsUrl;

    public RiskRadarController(RiskRadarService riskRadarService) {
        this.riskRadarService = riskRadarService;
    }

    @GetMapping("/zones")
    public List<RiskResult> getZoneRisks(HttpServletRequest request) {

        ZoneRecord[] zones = fetch(
                base(wildlifeUrl) + "/api/zones",
                request,
                ZoneRecord[].class
        );

        IncidentRecord[] incidents = fetch(
                base(operationsUrl) + "/api/incidents",
                request,
                IncidentRecord[].class
        );

        PatrolRecord[] patrols = fetch(
                base(operationsUrl) + "/api/patrols",
                request,
                PatrolRecord[].class
        );

        Map<String, int[]> counts = new HashMap<>();

        for (ZoneRecord zone : zones) {
            if (zone != null && validId(zone.zoneId())) {
                counts.putIfAbsent(zone.zoneId().trim(), new int[4]);
            }
        }

        for (IncidentRecord incident : incidents) {
            if (incident == null || !isOpen(incident.status())) {
                continue;
            }

            int[] values = counts.get(incident.zoneId() == null
                    ? null : incident.zoneId().trim());

            if (values == null) {
                continue;
            }

            switch (normalize(incident.severity())) {
                case "CRITICAL" -> values[0]++;
                case "HIGH" -> values[1]++;
                case "MEDIUM" -> values[2]++;
                default -> {
                }
            }
        }

        for (PatrolRecord patrol : patrols) {
            if (patrol == null || !Boolean.TRUE.equals(patrol.overdue())
                    || !isOpen(patrol.status())) {
                continue;
            }

            int[] values = counts.get(patrol.zoneId() == null
                    ? null : patrol.zoneId().trim());

            if (values != null) {
                values[3]++;
            }
        }

        List<ZoneRiskInput> inputs = new ArrayList<>();

        for (ZoneRecord zone : zones) {
            if (zone == null || !validId(zone.zoneId())) {
                continue;
            }

            int[] values = counts.getOrDefault(
                    zone.zoneId().trim(),
                    new int[4]
            );

            inputs.add(new ZoneRiskInput(
                    zone.zoneId(),
                    zone.zoneCode(),
                    zone.name(),
                    values[0],
                    values[1],
                    values[2],
                    values[3],
                    Math.max(0, zone.waterWarnings() == null
                            ? 0 : zone.waterWarnings()),
                    Boolean.TRUE.equals(zone.endangeredSpeciesFlag())
            ));
        }

        return riskRadarService.calculateZones(inputs);
    }

    private <T> T fetch(
            String url,
            HttpServletRequest request,
            Class<T> responseType
    ) {
        HttpHeaders headers = new HttpHeaders();

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        try {
            T result = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType
            ).getBody();

            if (result == null) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "A required Risk Radar service returned an empty response."
                );
            }

            return result;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Risk Radar could not retrieve data from " + url
                            + ". Check that the required service is running and its API contract matches.",
                    exception
            );
        }
    }

    private String base(String url) {
        return url.replaceAll("/+$", "");
    }

    private boolean validId(String id) {
        return id != null && !id.isBlank();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toUpperCase().replaceAll("[\\s-]+", "_");
    }

    private boolean isOpen(String status) {
        String value = normalize(status);

        if (value.isBlank()) {
            return false;
        }

        return !Set.of(
                "RESOLVED",
                "CLOSED",
                "CANCELLED",
                "CANCELED",
                "COMPLETED",
                "DISMISSED"
        ).contains(value);
    }

    public record ZoneRecord(
            String zoneId,
            String zoneCode,
            String name,
            Integer waterWarnings,
            Boolean endangeredSpeciesFlag
    ) {
    }

    public record IncidentRecord(
            String incidentId,
            String zoneId,
            String severity,
            String status
    ) {
    }

    public record PatrolRecord(
            String patrolId,
            String zoneId,
            String status,
            Boolean overdue
    ) {
    }
}