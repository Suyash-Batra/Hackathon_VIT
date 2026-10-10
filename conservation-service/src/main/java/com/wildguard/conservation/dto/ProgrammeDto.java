package com.wildguard.conservation.dto;

import java.time.LocalDate;
import java.util.List;

import com.wildguard.conservation.entity.ProgrammeStatus;

// What we send out for a programme.
// completedQuantity and progressPercent are calculated, they are not columns in the table.
public class ProgrammeDto {

    private Long id;
    private String programmeCode;
    private String name;
    private String description;
    private Integer targetQuantity;
    private String unit;
    private long completedQuantity;
    private int progressPercent;
    private LocalDate startDate;
    private LocalDate dueDate;
    private ProgrammeStatus status;
    private boolean overdue;
    private String ownerStaffName;
    private String team;
    private List<ActivityDto> activities;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProgrammeCode() {
        return programmeCode;
    }

    public void setProgrammeCode(String programmeCode) {
        this.programmeCode = programmeCode;
    }

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

    public long getCompletedQuantity() {
        return completedQuantity;
    }

    public void setCompletedQuantity(long completedQuantity) {
        this.completedQuantity = completedQuantity;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
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

    public boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
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

    public List<ActivityDto> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDto> activities) {
        this.activities = activities;
    }
}
