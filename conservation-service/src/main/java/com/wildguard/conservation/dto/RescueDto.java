package com.wildguard.conservation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.wildguard.conservation.entity.ReleaseReviewStatus;
import com.wildguard.conservation.entity.RescueStage;

// What we send out for a rescue case. DTO = data transfer object.
// We dont send the entity directly, we copy the needed fields into this class.
public class RescueDto {

    private Long id;
    private String rescueCode;
    private String incidentId;
    private String incidentCode;
    private String animalId;
    private String animalCode;
    private String assignedStaffName;
    private RescueStage stage;
    private boolean active;
    private String summary;
    private String destination;
    private String outcome;
    private ReleaseReviewStatus releaseReviewStatus;
    private String releaseReviewNotes;
    private String releaseReviewedByName;
    private LocalDateTime releaseReviewedAt;
    private String openedByName;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private long daysOpen;

    // these tell the page which buttons to show for the logged in user
    private List<RescueStage> allowedNextStages;
    private boolean canAddTreatment;
    private boolean canRequestReview;
    private boolean canDecideReview;

    // only filled when we open one case (detail page), empty in the list page
    private List<TimelineEntry> timeline;
    private List<AllocationDto> allocations;

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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public long getDaysOpen() {
        return daysOpen;
    }

    public void setDaysOpen(long daysOpen) {
        this.daysOpen = daysOpen;
    }

    public List<RescueStage> getAllowedNextStages() {
        return allowedNextStages;
    }

    public void setAllowedNextStages(List<RescueStage> allowedNextStages) {
        this.allowedNextStages = allowedNextStages;
    }

    public boolean getCanAddTreatment() {
        return canAddTreatment;
    }

    public void setCanAddTreatment(boolean canAddTreatment) {
        this.canAddTreatment = canAddTreatment;
    }

    public boolean getCanRequestReview() {
        return canRequestReview;
    }

    public void setCanRequestReview(boolean canRequestReview) {
        this.canRequestReview = canRequestReview;
    }

    public boolean getCanDecideReview() {
        return canDecideReview;
    }

    public void setCanDecideReview(boolean canDecideReview) {
        this.canDecideReview = canDecideReview;
    }

    public List<TimelineEntry> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEntry> timeline) {
        this.timeline = timeline;
    }

    public List<AllocationDto> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationDto> allocations) {
        this.allocations = allocations;
    }
}
