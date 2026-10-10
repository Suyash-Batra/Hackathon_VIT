package com.wildguard.conservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Officer approves (true) or rejects (false) the release.
public class ReviewDecisionRequest {

    @NotNull
    private Boolean approved;
    @Size(max = 1000)
    private String notes;

    // getters and setters

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
