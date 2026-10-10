package com.wildguard.wildlife.controller;

import com.wildguard.wildlife.entity.*;
import com.wildguard.wildlife.service.WildlifeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WildlifeController {

    private final WildlifeService wildlifeService;

    @Autowired
    public WildlifeController(WildlifeService wildlifeService) {
        this.wildlifeService = wildlifeService;
    }

    // Animals
    @GetMapping("/animals")
    public ResponseEntity<List<Animal>> getAnimals(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long speciesId,
            @RequestParam(required = false) String healthStatus) {
        return ResponseEntity.ok(wildlifeService.getAllAnimals(zoneId, speciesId, healthStatus));
    }

    @GetMapping("/animals/{id}")
    public ResponseEntity<Animal> getAnimalById(@PathVariable Long id) {
        return ResponseEntity.ok(wildlifeService.getAnimalById(id));
    }

    // Species
    @GetMapping("/species")
    public ResponseEntity<List<Species>> getSpecies() {
        return ResponseEntity.ok(wildlifeService.getAllSpecies());
    }

    @PostMapping("/species")
    public ResponseEntity<Species> createSpecies(@RequestBody Species species) {
        return new ResponseEntity<>(wildlifeService.saveSpecies(species), HttpStatus.CREATED);
    }

    // Areas & Zones
    @GetMapping("/areas")
    public ResponseEntity<List<ProtectedArea>> getAreas() {
        return ResponseEntity.ok(wildlifeService.getAllAreas());
    }

    @PostMapping("/areas")
    public ResponseEntity<ProtectedArea> createArea(@RequestBody ProtectedArea area) {
        return new ResponseEntity<>(wildlifeService.saveArea(area), HttpStatus.CREATED);
    }

    @GetMapping("/zones")
    public ResponseEntity<List<Zone>> getZones() {
        return ResponseEntity.ok(wildlifeService.getAllZones());
    }

    @PostMapping("/zones")
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return new ResponseEntity<>(wildlifeService.saveZone(zone), HttpStatus.CREATED);
    }

    @PostMapping("/habitats")
    public ResponseEntity<Habitat> createHabitat(@RequestBody Habitat habitat) {
        return new ResponseEntity<>(wildlifeService.saveHabitat(habitat), HttpStatus.CREATED);
    }

    @GetMapping("/water-sources")
    public ResponseEntity<List<WaterSource>> getWaterSources() {
        return ResponseEntity.ok(wildlifeService.getAllWaterSources());
    }

    @PostMapping("/water-sources")
    public ResponseEntity<WaterSource> createWaterSource(@RequestBody WaterSource waterSource) {
        return new ResponseEntity<>(wildlifeService.saveWaterSource(waterSource), HttpStatus.CREATED);
    }

    
 // GET /api/habitats -> Re-evaluates and returns updated conditions
    @GetMapping("/habitats")
    public List<Habitat> getHabitats() {
        return habitatAssessmentService.reevaluateAllHabitats();
    }

    // PATCH /api/water-sources/{id}/status -> Changes water status and recalculates habitat
    @PatchMapping("/water-sources/{id}/status")
    public ResponseEntity<WaterSource> updateWaterStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        WaterSource ws = waterSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water source not found"));

        ws.setStatus(status.toUpperCase());
        ws.setLastCheckedAt(OffsetDateTime.now());
        WaterSource saved = waterSourceRepository.save(ws);

        // Automatically update the zone's habitat condition
        habitatAssessmentService.reevaluateZoneHabitat(saved.getZoneId());

        return ResponseEntity.ok(saved);
    }

    // POST /api/animals -> Saves new specimen and recalculates habitat
    @PostMapping("/animals")
    public ResponseEntity<Animal> createAnimal(@RequestBody Animal animal) {
        animal.setLastObservedAt(OffsetDateTime.now());
        Animal saved = animalRepository.save(animal);

        if (saved.getZoneId() != null) {
            habitatAssessmentService.reevaluateZoneHabitat(saved.getZoneId());
        }

        return ResponseEntity.ok(saved);
    }
}