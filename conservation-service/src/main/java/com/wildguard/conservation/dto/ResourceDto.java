package com.wildguard.conservation.dto;

import java.util.List;

import com.wildguard.conservation.entity.ResourceCategory;
import com.wildguard.conservation.entity.ResourceStatus;

// What we send out for a vehicle or equipment.
public class ResourceDto {

    private Long id;
    private String resourceCode;
    private ResourceCategory category;
    private String name;
    private ResourceStatus status;
    private String notes;

    // null if the resource is free
    private AllocationDto currentAllocation;

    // full history, only on the detail page
    private List<AllocationDto> allocations;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AllocationDto getCurrentAllocation() {
        return currentAllocation;
    }

    public void setCurrentAllocation(AllocationDto currentAllocation) {
        this.currentAllocation = currentAllocation;
    }

    public List<AllocationDto> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationDto> allocations) {
        this.allocations = allocations;
    }
}
