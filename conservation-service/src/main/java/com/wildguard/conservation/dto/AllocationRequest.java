package com.wildguard.conservation.dto;

import com.wildguard.conservation.entity.AllocationTarget;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Data for giving a resource to a patrol or rescue case.
// override = true means "take it from the current user and give it to the new one" (only officer/admin).
public class AllocationRequest {

    @NotNull
    private Long resourceId;
    @NotNull
    private AllocationTarget targetType;
    @NotNull
    private String targetId;
    @Size(max = 500)
    private String notes;
    private Boolean override;

    // getters and setters

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getOverride() {
        return override;
    }

    public void setOverride(Boolean override) {
        this.override = override;
    }
}
