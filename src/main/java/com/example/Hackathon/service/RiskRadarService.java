package com.example.Hackathon.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RiskRadarService {

    public List<RiskResult> calculateZones(List<ZoneRiskInput> zones) {
        if (zones == null) {
            return List.of();
        }

        return zones.stream()
                .filter(Objects::nonNull)
                .map(this::calculateZone)
                .sorted(Comparator.comparingInt(RiskResult::score).reversed())
                .toList();
    }

    private RiskResult calculateZone(ZoneRiskInput zone) {
        int critical = Math.max(0, zone.openCriticalIncidents());
        int high = Math.max(0, zone.openHighIncidents());
        int medium = Math.max(0, zone.openMediumIncidents());
        int overdue = Math.max(0, zone.overduePatrols());
        int water = Math.max(0, zone.waterWarnings());
        int endangered = zone.endangeredSpeciesFlag() ? 1 : 0;

        int criticalPoints = critical * 45;
        int highPoints = high * 25;
        int mediumPoints = medium * 10;
        int patrolPoints = overdue * 10;
        int waterPoints = water * 10;
        int speciesPoints = endangered * 10;

        int total = criticalPoints + highPoints + mediumPoints
                + patrolPoints + waterPoints + speciesPoints;

        int score = Math.min(100, total);

        String level;

        if (score >= 75) {
            level = "CRITICAL";
        } else if (score >= 50) {
            level = "HIGH";
        } else if (score >= 25) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        Map<String, Integer> contributions = new LinkedHashMap<>();
        contributions.put("criticalIncidents", criticalPoints);
        contributions.put("highIncidents", highPoints);
        contributions.put("mediumIncidents", mediumPoints);
        contributions.put("overduePatrols", patrolPoints);
        contributions.put("waterWarnings", waterPoints);
        contributions.put("endangeredSpecies", speciesPoints);

        List<String> reasons = new ArrayList<>();

        if (critical > 0) {
            reasons.add(critical + " open critical incident(s): +" + criticalPoints);
        }

        if (high > 0) {
            reasons.add(high + " open high-severity incident(s): +" + highPoints);
        }

        if (medium > 0) {
            reasons.add(medium + " open medium-severity incident(s): +" + mediumPoints);
        }

        if (overdue > 0) {
            reasons.add(overdue + " overdue patrol(s): +" + patrolPoints);
        }

        if (water > 0) {
            reasons.add(water + " water-source warning(s): +" + waterPoints);
        }

        if (endangered == 1) {
            reasons.add("Endangered-species protection flag is present: +10");
        }

        if (reasons.isEmpty()) {
            reasons.add("No scoring risk factors are currently recorded for this zone.");
        }

        String recommendation;

        switch (level) {
            case "CRITICAL":
                recommendation = "Request immediate officer review and prioritize a patrol.";
                break;
            case "HIGH":
                recommendation = "Recommend a priority patrol and review unresolved incidents.";
                break;
            case "MEDIUM":
                recommendation = "Review the contributing alerts and check the next patrol schedule.";
                break;
            default:
                recommendation = "Continue routine monitoring and scheduled patrols.";
        }

        return new RiskResult(
                zone.zoneId(),
                zone.zoneCode(),
                zone.name(),
                score,
                total,
                level,
                contributions,
                reasons,
                recommendation
        );
    }

    public record ZoneRiskInput(
            String zoneId,
            String zoneCode,
            String name,
            int openCriticalIncidents,
            int openHighIncidents,
            int openMediumIncidents,
            int overduePatrols,
            int waterWarnings,
            boolean endangeredSpeciesFlag
    ) {
    }

    public record RiskResult(
            String zoneId,
            String zoneCode,
            String name,
            int score,
            int uncappedScore,
            String level,
            Map<String, Integer> factorContributions,
            List<String> reasons,
            String recommendedAction
    ) {
    }
}