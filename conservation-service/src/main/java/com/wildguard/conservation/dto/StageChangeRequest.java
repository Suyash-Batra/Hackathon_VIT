package com.wildguard.conservation.dto;

import com.wildguard.conservation.entity.RescueStage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Data for moving a rescue case to the next stage.
public class StageChangeRequest {

    @NotNull
    private RescueStage stage;
    @Size(max = 1000)
    private String notes;
    @Size(max = 200)
    private String destination;
    @Size(max = 500)
    private String outcome;

    // getters and setters

    public RescueStage getStage() {
        return stage;
    }

    public void setStage(RescueStage stage) {
        this.stage = stage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
