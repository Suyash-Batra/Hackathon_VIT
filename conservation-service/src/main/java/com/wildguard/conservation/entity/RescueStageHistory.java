package com.wildguard.conservation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Every time the stage of a rescue case changes we add one row here.
// This is what the timeline on the rescue page is made from.
// If oldStage and newStage are the same it is a release review entry, not a stage change.
@Entity
@Table(name = "rescue_stage_history")
public class RescueStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // many history rows belong to one rescue case
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rescue_case_id", nullable = false)
    private RescueCase rescueCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_stage")
    private RescueStage oldStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_stage", nullable = false)
    private RescueStage newStage;

    @Column(nullable = false)
    private String action;

    private String notes;


    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RescueCase getRescueCase() {
        return rescueCase;
    }

    public void setRescueCase(RescueCase rescueCase) {
        this.rescueCase = rescueCase;
    }

    public RescueStage getOldStage() {
        return oldStage;
    }

    public void setOldStage(RescueStage oldStage) {
        this.oldStage = oldStage;
    }

    public RescueStage getNewStage() {
        return newStage;
    }

    public void setNewStage(RescueStage newStage) {
        this.newStage = newStage;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
