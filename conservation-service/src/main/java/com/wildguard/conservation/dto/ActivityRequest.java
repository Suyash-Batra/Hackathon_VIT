package com.wildguard.conservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Data for recording completed work in a programme.
public class ActivityRequest {

    @NotBlank
    @Size(max = 500)
    private String description;
    @NotNull
    @Min(1)
    @Max(1000000)
    private Integer quantityCompleted;
    private LocalDateTime completedAt;

    // getters and setters

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
}
