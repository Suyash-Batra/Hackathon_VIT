package com.wildguard.conservation.dto;

import java.time.LocalDate;

import com.wildguard.conservation.entity.ProgrammeStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Data from the add/edit programme form.
public class ProgrammeRequest {

    @NotBlank
    @Size(max = 150)
    private String name;
    @Size(max = 1000)
    private String description;
    @NotNull
    @Min(1)
    @Max(1000000)
    private Integer targetQuantity;
    @Size(max = 40)
    private String unit;
    private LocalDate startDate;
    private LocalDate dueDate;
    @NotNull
    private ProgrammeStatus status;
    @Size(max = 120)
    private String ownerStaffName;
    @Size(max = 60)
    private String team;

    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(Integer targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ProgrammeStatus getStatus() {
        return status;
    }

    public void setStatus(ProgrammeStatus status) {
        this.status = status;
    }

    public String getOwnerStaffName() {
        return ownerStaffName;
    }

    public void setOwnerStaffName(String ownerStaffName) {
        this.ownerStaffName = ownerStaffName;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
