package com.wildguard.conservation.dto;

import java.time.LocalDateTime;

import com.wildguard.conservation.entity.AllocationTarget;
import com.wildguard.conservation.entity.ResourceCategory;

// What we send out for one allocation (which resource is given to what).
public class AllocationDto {

    private Long id;
    private Long resourceId;
    private String resourceCode;
    private String resourceName;
    private ResourceCategory category;
    private AllocationTarget targetType;
    private String targetId;
    private String targetCode;
    private LocalDateTime allocatedAt;
    private String allocatedByName;
    private LocalDateTime releasedAt;
    private String releasedByName;
    private String notes;
    private boolean overrideUsed;

    // true = still in use
    private boolean active;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
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

    public boolean getOverrideUsed() {
        return overrideUsed;
    }

    public void setOverrideUsed(boolean overrideUsed) {
        this.overrideUsed = overrideUsed;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
