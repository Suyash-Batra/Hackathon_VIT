package com.wildguard.conservation.dto;

import java.util.List;

import com.wildguard.conservation.dto.AttentionItem;
import com.wildguard.conservation.dto.LabelValue;

// Numbers from my service for a dashboard. They come from GET /api/conservation/summary.
public class ConservationSummary {

    private long activeRescues;
    private long pendingReleaseReviews;
    private long allocatedResources;
    private long availableResources;
    private long maintenanceResources;
    private List<LabelValue> rescuesByStage;
    private List<LabelValue> programmeProgress;
    private List<AttentionItem> attention;

    // getters and setters

    public long getActiveRescues() {
        return activeRescues;
    }

    public void setActiveRescues(long activeRescues) {
        this.activeRescues = activeRescues;
    }

    public long getPendingReleaseReviews() {
        return pendingReleaseReviews;
    }

    public void setPendingReleaseReviews(long pendingReleaseReviews) {
        this.pendingReleaseReviews = pendingReleaseReviews;
    }

    public long getAllocatedResources() {
        return allocatedResources;
    }

    public void setAllocatedResources(long allocatedResources) {
        this.allocatedResources = allocatedResources;
    }

    public long getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(long availableResources) {
        this.availableResources = availableResources;
    }

    public long getMaintenanceResources() {
        return maintenanceResources;
    }

    public void setMaintenanceResources(long maintenanceResources) {
        this.maintenanceResources = maintenanceResources;
    }

    public List<LabelValue> getRescuesByStage() {
        return rescuesByStage;
    }

    public void setRescuesByStage(List<LabelValue> rescuesByStage) {
        this.rescuesByStage = rescuesByStage;
    }

    public List<LabelValue> getProgrammeProgress() {
        return programmeProgress;
    }

    public void setProgrammeProgress(List<LabelValue> programmeProgress) {
        this.programmeProgress = programmeProgress;
    }

    public List<AttentionItem> getAttention() {
        return attention;
    }

    public void setAttention(List<AttentionItem> attention) {
        this.attention = attention;
    }
}
