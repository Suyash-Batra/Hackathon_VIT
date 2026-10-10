package com.wildguard.conservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data that comes from the "register rescue case" form.
// incident and animal are optional because sometimes we dont know them yet.
public class RescueRequest {

    private String incidentId;
    private String animalId;
    @Size(max = 120)
    private String assignedStaffName;
    @NotBlank
    @Size(max = 1000)
    private String summary;

    // getters and setters

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
