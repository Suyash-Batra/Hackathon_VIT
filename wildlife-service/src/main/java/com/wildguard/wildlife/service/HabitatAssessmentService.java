package com.wildguard.wildlife.service;

import com.wildguard.wildlife.entity.Animal;
import com.wildguard.wildlife.entity.Habitat;
import com.wildguard.wildlife.entity.WaterSource;
import com.wildguard.wildlife.repository.AnimalRepository;
import com.wildguard.wildlife.repository.HabitatRepository;
import com.wildguard.wildlife.repository.WaterSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class HabitatAssessmentService {

    private final HabitatRepository habitatRepository;
    private final WaterSourceRepository waterSourceRepository;
    private final AnimalRepository animalRepository;

    public HabitatAssessmentService(HabitatRepository habitatRepository,
                                    WaterSourceRepository waterSourceRepository,
                                    AnimalRepository animalRepository) {
        this.habitatRepository = habitatRepository;
        this.waterSourceRepository = waterSourceRepository;
        this.animalRepository = animalRepository;
    }

    /**
     * Re-evaluates and persists the habitat status for a specific zone.
     */
    @Transactional
    public Habitat reevaluateZoneHabitat(Long zoneId) {
        Habitat habitat = habitatRepository.findByZoneId(zoneId)
                .orElseGet(() -> {
                    Habitat newHabitat = new Habitat();
                    newHabitat.setZoneId(zoneId);
                    newHabitat.setHabitatType("Mixed Reserve Terrain");
                    newHabitat.setLastInspectedAt(OffsetDateTime.now());
                    return newHabitat;
                });

        int stressScore = 0;

        // 1. Water Stress Factor
        List<WaterSource> waters = waterSourceRepository.findByZoneId(zoneId);
        if (!waters.isEmpty()) {
            long lowCount = waters.stream()
                    .filter(w -> "LOW".equalsIgnoreCase(w.getStatus()))
                    .count();
            double lowRatio = (double) lowCount / waters.size();

            if (lowRatio >= 0.6) {
                stressScore += 40; // Severe water shortage
            } else if (lowRatio > 0.0) {
                stressScore += 20; // Moderate water shortage
            }
        }

        // 2. Animal Health & Distress Signals
        List<Animal> animals = animalRepository.findByZoneId(zoneId);
        if (!animals.isEmpty()) {
            long criticalOrInjured = animals.stream()
                    .filter(a -> "CRITICAL".equalsIgnoreCase(a.getHealthStatus()) 
                              || "INJURED".equalsIgnoreCase(a.getHealthStatus()))
                    .count();
            
            if (criticalOrInjured >= 2) {
                stressScore += 30; // High distress signal / snare risk
            } else if (criticalOrInjured == 1) {
                stressScore += 15;
            }

            // High pressure / over-saturation
            if (animals.size() > 10) {
                stressScore += 10;
            }
        }

        // 3. Inspection Freshness Factor
        if (habitat.getLastInspectedAt() != null) {
            long daysOld = Duration.between(habitat.getLastInspectedAt(), OffsetDateTime.now()).toDays();
            if (daysOld > 30) {
                stressScore += 15; // Outdated data penalty
            }
        }

        // Compute Condition Status based on cumulative stress
        if (stressScore >= 50) {
            habitat.setConditionStatus("CRITICAL");
        } else if (stressScore >= 25) {
            habitat.setConditionStatus("DEGRADED");
        } else if (stressScore > 0) {
            habitat.setConditionStatus("STABLE");
        } else {
            habitat.setConditionStatus("EXCELLENT");
        }

        return habitatRepository.save(habitat);
    }

    @Transactional
    public List<Habitat> reevaluateAllHabitats() {
        List<Habitat> habitats = habitatRepository.findAll();
        for (Habitat h : habitats) {
            reevaluateZoneHabitat(h.getZoneId());
        }
        return habitatRepository.findAll();
    }
}