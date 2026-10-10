package com.wildguard.wildlife.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "water_sources")
public class WaterSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "last_checked_at")
    private OffsetDateTime lastCheckedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public WaterSource() {
        super();
    }

    public WaterSource(Long id, Long zoneId, String name, String status, OffsetDateTime lastCheckedAt, String notes) {
        super();
        this.id = id;
        this.zoneId = zoneId;
        this.name = name;
        this.status = status;
        this.lastCheckedAt = lastCheckedAt;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "WaterSource [id=" + id + ", zoneId=" + zoneId + ", name=" + name + ", status=" + status
                + ", lastCheckedAt=" + lastCheckedAt + ", notes=" + notes + "]";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(OffsetDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}