package com.wildguard.conservation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// One rescue case = one row in the rescue_cases table.
// incidentId and animalId are ids from the OTHER services (operations and wildlife), so there is
// no foreign key here. They are saved as text because each service decides how its ids look.
// We also save the code so we can show it without calling them again.
@Entity
@Table(name = "rescue_cases")
public class RescueCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rescue_code", nullable = false, unique = true)
    private String rescueCode;

    @Column(name = "incident_id")
    private String incidentId;

    @Column(name = "incident_code")
    private String incidentCode;

    @Column(name = "animal_id")
    private String animalId;

    @Column(name = "animal_code")
    private String animalCode;


    @Column(name = "assigned_staff_name")
    private String assignedStaffName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescueStage stage;

    @Column(nullable = false)
    private String summary;

    private String destination;

    private String outcome;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_review_status", nullable = false)
    private ReleaseReviewStatus releaseReviewStatus;

    @Column(name = "release_review_notes")
    private String releaseReviewNotes;

    @Column(name = "release_reviewed_by_name")
    private String releaseReviewedByName;

    @Column(name = "release_reviewed_at")
    private LocalDateTime releaseReviewedAt;


    @Column(name = "opened_by_name")
    private String openedByName;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRescueCode() {
        return rescueCode;
    }

    public void setRescueCode(String rescueCode) {
        this.rescueCode = rescueCode;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getIncidentCode() {
        return incidentCode;
    }

    public void setIncidentCode(String incidentCode) {
        this.incidentCode = incidentCode;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getAnimalCode() {
        return animalCode;
    }

    public void setAnimalCode(String animalCode) {
        this.animalCode = animalCode;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    public RescueStage getStage() {
        return stage;
    }

    public void setStage(RescueStage stage) {
        this.stage = stage;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public ReleaseReviewStatus getReleaseReviewStatus() {
        return releaseReviewStatus;
    }

    public void setReleaseReviewStatus(ReleaseReviewStatus releaseReviewStatus) {
        this.releaseReviewStatus = releaseReviewStatus;
    }

    public String getReleaseReviewNotes() {
        return releaseReviewNotes;
    }

    public void setReleaseReviewNotes(String releaseReviewNotes) {
        this.releaseReviewNotes = releaseReviewNotes;
    }

    public String getReleaseReviewedByName() {
        return releaseReviewedByName;
    }

    public void setReleaseReviewedByName(String releaseReviewedByName) {
        this.releaseReviewedByName = releaseReviewedByName;
    }

    public LocalDateTime getReleaseReviewedAt() {
        return releaseReviewedAt;
    }

    public void setReleaseReviewedAt(LocalDateTime releaseReviewedAt) {
        this.releaseReviewedAt = releaseReviewedAt;
    }

    public String getOpenedByName() {
        return openedByName;
    }

    public void setOpenedByName(String openedByName) {
        this.openedByName = openedByName;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
