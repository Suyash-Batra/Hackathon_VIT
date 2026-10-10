package com.wildguard.wildlife.service;

import com.wildguard.wildlife.entity.*;
import com.wildguard.wildlife.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class WildlifeService {

    private final AnimalRepository animalRepository;
    private final SpeciesRepository speciesRepository;
    private final ZoneRepository zoneRepository;
    private final ProtectedAreaRepository protectedAreaRepository;
    private final HabitatRepository habitatRepository;
    private final WaterSourceRepository waterSourceRepository;

    @Autowired
    public WildlifeService(AnimalRepository animalRepository,
                           SpeciesRepository speciesRepository,
                           ZoneRepository zoneRepository,
                           ProtectedAreaRepository protectedAreaRepository,
                           HabitatRepository habitatRepository,
                           WaterSourceRepository waterSourceRepository) {
        this.animalRepository = animalRepository;
        this.speciesRepository = speciesRepository;
        this.zoneRepository = zoneRepository;
        this.protectedAreaRepository = protectedAreaRepository;
        this.habitatRepository = habitatRepository;
        this.waterSourceRepository = waterSourceRepository;
    }

    public List<Animal> getAllAnimals(Long zoneId, Long speciesId, String healthStatus) {
        if (zoneId != null) return animalRepository.findByZoneId(zoneId);
        if (speciesId != null) return animalRepository.findBySpeciesId(speciesId);
        if (healthStatus != null && !healthStatus.trim().isEmpty()) {
            return animalRepository.findByHealthStatus(healthStatus.toUpperCase());
        }
        return animalRepository.findAll();
    }

    public Animal getAnimalById(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal not found with id: " + id));
    }

    @Transactional
    public Animal saveAnimal(Animal animal) {
        if (animal.getLastObservedAt() == null) {
            animal.setLastObservedAt(OffsetDateTime.now());
        }
        Animal saved = animalRepository.save(animal);

        // Recalculate habitat ecosystem status when specimen data changes
        if (saved.getZoneId() != null) {
            reevaluateZoneHabitat(saved.getZoneId());
        }
        return saved;
    }

    public List<Species> getAllSpecies() {
        return speciesRepository.findAll();
    }

    public Species saveSpecies(Species species) {
        return speciesRepository.save(species);
    }

    public List<ProtectedArea> getAllAreas() {
        return protectedAreaRepository.findAll();
    }

    public ProtectedArea saveArea(ProtectedArea area) {
        return protectedAreaRepository.save(area);
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone saveZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    @Transactional
    public List<Habitat> getAllHabitats() {
        List<Habitat> list = habitatRepository.findAll();
        for (Habitat h : list) {
            reevaluateZoneHabitat(h.getZoneId());
        }
        return habitatRepository.findAll();
    }

    public Habitat saveHabitat(Habitat habitat) {
        if (habitat.getLastInspectedAt() == null) {
            habitat.setLastInspectedAt(OffsetDateTime.now());
        }
        return habitatRepository.save(habitat);
    }

    @Transactional
    public Habitat reevaluateZoneHabitat(Long zoneId) {
        List<Habitat> existingList = habitatRepository.findByZoneId(zoneId);
        Habitat habitat;

        if (existingList != null && !existingList.isEmpty()) {
            habitat = existingList.get(0);
        } else {
            habitat = new Habitat();
            habitat.setZoneId(zoneId);
            habitat.setHabitatType("Mixed Reserve Terrain");
            habitat.setLastInspectedAt(OffsetDateTime.now());
        }

        int stressScore = 0;

        List<WaterSource> waters = waterSourceRepository.findByZoneId(zoneId);
        if (!waters.isEmpty()) {
            long lowWaterCount = waters.stream()
                    .filter(w -> "LOW".equalsIgnoreCase(w.getStatus()))
                    .count();
            double lowRatio = (double) lowWaterCount / waters.size();

            if (lowRatio >= 0.6) {
                stressScore += 40;
            } else if (lowRatio > 0.0) {
                stressScore += 20;
            }
        }

        List<Animal> animals = animalRepository.findByZoneId(zoneId);
        if (!animals.isEmpty()) {
            long distressedCount = animals.stream()
                    .filter(a -> "CRITICAL".equalsIgnoreCase(a.getHealthStatus())
                              || "INJURED".equalsIgnoreCase(a.getHealthStatus()))
                    .count();

            if (distressedCount >= 2) {
                stressScore += 30;
            } else if (distressedCount == 1) {
                stressScore += 15;
            }

            if (animals.size() > 10) {
                stressScore += 10;
            }
        }

        if (habitat.getLastInspectedAt() != null) {
            long daysSinceInspection = Duration.between(habitat.getLastInspectedAt(), OffsetDateTime.now()).toDays();
            if (daysSinceInspection > 30) {
                stressScore += 15;
            }
        }

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

    public List<WaterSource> getAllWaterSources() {
        return waterSourceRepository.findAll();
    }

    @Transactional
    public WaterSource saveWaterSource(WaterSource waterSource) {
        if (waterSource.getLastCheckedAt() == null) {
            waterSource.setLastCheckedAt(OffsetDateTime.now());
        }
        WaterSource saved = waterSourceRepository.save(waterSource);
        reevaluateZoneHabitat(saved.getZoneId());
        return saved;
    }

    @Transactional
    public WaterSource updateWaterStatus(Long id, String status) {
        WaterSource ws = waterSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WaterSource not found with id: " + id));

        ws.setStatus(status.toUpperCase());
        ws.setLastCheckedAt(OffsetDateTime.now());
        WaterSource saved = waterSourceRepository.save(ws);

        reevaluateZoneHabitat(saved.getZoneId());

        return saved;
    }
}