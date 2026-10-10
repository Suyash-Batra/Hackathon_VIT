package com.wildguard.conservation.dto;

import jakarta.validation.constraints.Size;

// Rescue officer asks for a release review.
public class ReviewRequest {

    @Size(max = 1000)
    private String notes;

    // getters and setters

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
