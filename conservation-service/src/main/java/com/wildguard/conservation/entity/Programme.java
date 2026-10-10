package com.wildguard.conservation.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

// A conservation programme with a target, for example "restore 20 water sources".
// Notice there is NO progress column. Progress is calculated from the activities.
@Entity
@Table(name = "programmes")
public class Programme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "programme_code", nullable = false, unique = true)
    private String programmeCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "target_quantity", nullable = false)
    private Integer targetQuantity;

    private String unit;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgrammeStatus status;

    @Column(name = "owner_staff_name")
    private String ownerStaffName;

    private String team;

    // not columns: calculated by ProgrammeService every time (progress is never saved)
    @Transient
    private long completedQuantity;
    @Transient
    private int progressPercent;
    @Transient
    private boolean overdue;
    @Transient
    private List<ProgrammeActivity> activities = new ArrayList<>();

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

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public List<ProgrammeActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<ProgrammeActivity> activities) {
        this.activities = activities;
    }

    public enum ProgrammeStatus {
        PLANNED, ACTIVE, ON_HOLD, COMPLETED
    }
}
