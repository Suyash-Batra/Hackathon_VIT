package com.wildguard.conservation.dto;

import java.time.LocalDateTime;

// One activity of a programme.
public class ActivityDto {

    private Long id;
    private String description;
    private Integer quantityCompleted;
    private LocalDateTime completedAt;
    private String recordedByName;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantityCompleted() {
        return quantityCompleted;
    }

    public void setQuantityCompleted(Integer quantityCompleted) {
        this.quantityCompleted = quantityCompleted;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }
}
