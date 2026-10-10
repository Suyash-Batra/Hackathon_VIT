package com.wildguard.operations.risk;

import com.wildguard.operations.entity.Incident;
import com.wildguard.operations.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskRadarService {
    private final IncidentRepository repository;
    public RiskRadarService(IncidentRepository repository) { this.repository = repository; }

    public List<RiskZone> getZones() {
        Map<String, List<Incident>> grouped = repository.findAll().stream()
            .collect(Collectors.groupingBy(i -> clean(i.getLocation()), TreeMap::new, Collectors.toList()));
        List<RiskZone> zones = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<Incident> incidents = entry.getValue();
            int score = 0;
            long critical = incidents.stream().filter(i -> eq(i.getSeverity(), "CRITICAL")).count();
            long high = incidents.stream().filter(i -> eq(i.getSeverity(), "HIGH")).count();
            long medium = incidents.stream().filter(i -> eq(i.getSeverity(), "MEDIUM")).count();
            long open = incidents.stream().filter(i -> !eq(i.getStatus(), "RESOLVED") && !eq(i.getStatus(), "CLOSED")).count();
            score += (int)Math.min(100, critical * 35 + high * 20 + medium * 10 + Math.max(0, incidents.size() - critical - high - medium) * 5 + open * 5);
            score = Math.min(100, score);
            List<String> reasons = new ArrayList<>();
            if (critical > 0) reasons.add(critical + " critical incident(s)");
            if (high > 0) reasons.add(high + " high-severity incident(s)");
            if (medium > 0) reasons.add(medium + " medium-severity incident(s)");
            if (open > 0) reasons.add(open + " incident(s) still open");
            if (reasons.isEmpty()) reasons.add("No active incident indicators");
            String level = score >= 75 ? "CRITICAL" : score >= 50 ? "HIGH" : score >= 25 ? "MEDIUM" : "LOW";
            String action = score >= 75 ? "Dispatch an authorized officer and review the zone immediately." : score >= 50 ? "Prioritize patrol coverage and review open incidents." : score >= 25 ? "Schedule a patrol and monitor new reports." : "Continue routine monitoring.";
            zones.add(new RiskZone(entry.getKey(), entry.getKey().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-"), score, level, reasons, action, incidents.size()));
        }
        zones.sort(Comparator.comparingInt(RiskZone::score).reversed());
        return zones;
    }
    private static boolean eq(String a, String b) { return a != null && a.equalsIgnoreCase(b); }
    private static String clean(String value) { return value == null || value.isBlank() ? "Unspecified location" : value.trim(); }
}
