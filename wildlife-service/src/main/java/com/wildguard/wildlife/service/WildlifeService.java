package com.wildguard.wildlife.service;

import com.wildguard.wildlife.entity.*;
import com.wildguard.wildlife.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Animal saveAnimal(Animal animal) {
        if (animal.getLastObservedAt() == null) {
            animal.setLastObservedAt(OffsetDateTime.now());
        }
        return animalRepository.save(animal);
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

    // --- Zones ---
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone saveZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    // --- Habitats ---
    public List<Habitat> getAllHabitats() {
        return habitatRepository.findAll();
    }

    public Habitat saveHabitat(Habitat habitat) {
        if (habitat.getLastInspectedAt() == null) {
            habitat.setLastInspectedAt(OffsetDateTime.now());
        }
        return habitatRepository.save(habitat);
    }

    public List<WaterSource> getAllWaterSources() {
        return waterSourceRepository.findAll();
    }

    public WaterSource saveWaterSource(WaterSource waterSource) {
        if (waterSource.getLastCheckedAt() == null) {
            waterSource.setLastCheckedAt(OffsetDateTime.now());
        }
        return waterSourceRepository.save(waterSource);
    }

    public WaterSource updateWaterStatus(Long id, String status) {
        WaterSource ws = waterSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WaterSource not found with id: " + id));
        ws.setStatus(status.toUpperCase());
        ws.setLastCheckedAt(OffsetDateTime.now());
        return waterSourceRepository.save(ws);
    }
}