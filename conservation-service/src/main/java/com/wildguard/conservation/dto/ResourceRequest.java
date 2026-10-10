package com.wildguard.conservation.dto;

import com.wildguard.conservation.entity.ResourceCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Data from the add/edit resource form.
public class ResourceRequest {

    @NotBlank
    @Size(max = 20)
    private String resourceCode;
    @NotNull
    private ResourceCategory category;
    @NotBlank
    @Size(max = 120)
    private String name;
    @Size(max = 500)
    private String notes;

    // getters and setters

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
