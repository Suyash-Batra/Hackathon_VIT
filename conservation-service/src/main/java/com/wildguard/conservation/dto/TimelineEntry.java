package com.wildguard.conservation.dto;

import java.time.LocalDateTime;

import com.wildguard.conservation.entity.RescueStage;

// One line of the rescue timeline shown on the rescue page.
// kind can be STAGE (stage changed), TREATMENT (treatment note) or REVIEW (release review).
public class TimelineEntry {

    private LocalDateTime at;
    private String kind;
    private String title;
    private RescueStage stage;
    private String person;
    private String notes;

    // getters and setters

    public LocalDateTime getAt() {
        return at;
    }

    public void setAt(LocalDateTime at) {
        this.at = at;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RescueStage getStage() {
        return stage;
    }

    public void setStage(RescueStage stage) {
        this.stage = stage;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
