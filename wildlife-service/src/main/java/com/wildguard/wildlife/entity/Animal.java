package com.wildguard.wildlife.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "animal_code", nullable = false, unique = true, length = 50)
    private String animalCode;

    @Column(name = "species_id", nullable = false)
    private Long speciesId;

    @Column(name = "estimated_age", length = 50)
    private String estimatedAge;

    @Column(name = "health_status", nullable = false, length = 50)
    private String healthStatus;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "last_observed_at")
    private OffsetDateTime lastObservedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Animal() {
        super();
    }

    public Animal(Long id, String animalCode, Long speciesId, String estimatedAge, String healthStatus,
                  Long zoneId, OffsetDateTime lastObservedAt, String notes) {
        super();
        this.id = id;
        this.animalCode = animalCode;
        this.speciesId = speciesId;
        this.estimatedAge = estimatedAge;
        this.healthStatus = healthStatus;
        this.zoneId = zoneId;
        this.lastObservedAt = lastObservedAt;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Animal [id=" + id + ", animalCode=" + animalCode + ", speciesId=" + speciesId
                + ", estimatedAge=" + estimatedAge + ", healthStatus=" + healthStatus + ", zoneId=" + zoneId
                + ", lastObservedAt=" + lastObservedAt + ", notes=" + notes + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnimalCode() {
        return animalCode;
    }

    public void setAnimalCode(String animalCode) {
        this.animalCode = animalCode;
    }

    public Long getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(Long speciesId) {
        this.speciesId = speciesId;
    }

    public String getEstimatedAge() {
        return estimatedAge;
    }

    public void setEstimatedAge(String estimatedAge) {
        this.estimatedAge = estimatedAge;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public OffsetDateTime getLastObservedAt() {
        return lastObservedAt;
    }

    public void setLastObservedAt(OffsetDateTime lastObservedAt) {
        this.lastObservedAt = lastObservedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}