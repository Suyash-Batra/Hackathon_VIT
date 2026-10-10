package com.wildguard.wildlife.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "habitats")
public class Habitat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "habitat_type", nullable = false, length = 100)
    private String habitatType;

    @Column(name = "condition_status", nullable = false, length = 50)
    private String conditionStatus;

    @Column(name = "last_inspected_at")
    private OffsetDateTime lastInspectedAt;

    public Habitat() {
        super();
    }

    public Habitat(Long id, Long zoneId, String habitatType, String conditionStatus, OffsetDateTime lastInspectedAt) {
        super();
        this.id = id;
        this.zoneId = zoneId;
        this.habitatType = habitatType;
        this.conditionStatus = conditionStatus;
        this.lastInspectedAt = lastInspectedAt;
    }

    @Override
    public String toString() {
        return "Habitat [id=" + id + ", zoneId=" + zoneId + ", habitatType=" + habitatType
                + ", conditionStatus=" + conditionStatus + ", lastInspectedAt=" + lastInspectedAt + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getHabitatType() {
        return habitatType;
    }

    public void setHabitatType(String habitatType) {
        this.habitatType = habitatType;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public OffsetDateTime getLastInspectedAt() {
        return lastInspectedAt;
    }

    public void setLastInspectedAt(OffsetDateTime lastInspectedAt) {
        this.lastInspectedAt = lastInspectedAt;
    }
}