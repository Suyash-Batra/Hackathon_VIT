package com.wildguard.conservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data for adding a treatment note.
public class TreatmentRequest {

    @NotBlank
    @Size(max = 500)
    private String treatment;
    @Size(max = 1000)
    private String notes;

    // getters and setters

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
