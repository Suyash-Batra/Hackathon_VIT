package com.wildguard.conservation.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

// A vehicle or equipment (table name is "resources").
// I named the class ResourceItem because Resource is already a class name in Spring.
@Entity
@Table(name = "resources")
public class ResourceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_code", nullable = false, unique = true)
    private String resourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceCategory category;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status;

    private String notes;

    // not columns: who has the resource now, and its allocation history (filled by the service)
    @Transient
    private ResourceAllocation currentAllocation;
    @Transient
    private List<ResourceAllocation> allocations = new ArrayList<>();

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

    public ResourceAllocation getCurrentAllocation() {
        return currentAllocation;
    }

    public void setCurrentAllocation(ResourceAllocation currentAllocation) {
        this.currentAllocation = currentAllocation;
    }

    public List<ResourceAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<ResourceAllocation> allocations) {
        this.allocations = allocations;
    }

    // AVAILABLE = free to use, ALLOCATED = somebody is using it, MAINTENANCE = under repair
    public enum ResourceStatus {
        AVAILABLE, ALLOCATED, MAINTENANCE
    }

    // type of resource
    public enum ResourceCategory {
        VEHICLE, GPS_DEVICE, CAMERA, RADIO, MEDICAL_KIT, OTHER
    }
}
