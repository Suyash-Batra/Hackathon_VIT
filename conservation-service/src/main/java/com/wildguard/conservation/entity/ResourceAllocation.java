package com.wildguard.conservation.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.wildguard.conservation.entity.ResourceItem.ResourceCategory;

// Says which resource is given to which patrol or rescue case.
// If releasedAt is null the resource is still in use (active allocation).
// One resource can have only ONE active allocation at a time.
@Entity
@Table(name = "resource_allocations")
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the full resource object is not sent in the JSON, only its id, code and name (see below)
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private ResourceItem resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AllocationTarget targetType;

    // id of the patrol or the rescue case
    @Column(name = "target_id", nullable = false)
    private String targetId;

    // like PAT-205 or RES-031, saved so we can show it easily
    @Column(name = "target_code")
    private String targetCode;

    @Column(name = "allocated_at", nullable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "allocated_by_name")
    private String allocatedByName;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "released_by_name")
    private String releasedByName;

    private String notes;

    @Column(name = "override_used", nullable = false)
    private Boolean overrideUsed;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceItem getResource() {
        return resource;
    }

    public void setResource(ResourceItem resource) {
        this.resource = resource;
    }

    public AllocationTarget getTargetType() {
        return targetType;
    }

    public void setTargetType(AllocationTarget targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(LocalDateTime allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public String getAllocatedByName() {
        return allocatedByName;
    }

    public void setAllocatedByName(String allocatedByName) {
        this.allocatedByName = allocatedByName;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public String getReleasedByName() {
        return releasedByName;
    }

    public void setReleasedByName(String releasedByName) {
        this.releasedByName = releasedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getOverrideUsed() {
        return overrideUsed;
    }

    public void setOverrideUsed(Boolean overrideUsed) {
        this.overrideUsed = overrideUsed;
    }

    // ---- extra values for the page, taken from the linked resource ----

    public Long getResourceId() {
        return resource.getId();
    }

    public String getResourceCode() {
        return resource.getResourceCode();
    }

    public String getResourceName() {
        return resource.getName();
    }

    public ResourceCategory getCategory() {
        return resource.getCategory();
    }

    // still in use = not given back yet
    public boolean isActive() {
        return releasedAt == null;
    }

    // a resource can be given to a patrol or to a rescue case
    public enum AllocationTarget {
        PATROL, RESCUE_CASE
    }
}
