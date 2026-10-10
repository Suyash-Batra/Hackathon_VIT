package com.wildguard.conservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.service.ActivityLog;
import com.wildguard.conservation.error.ApiException;
import com.wildguard.conservation.security.Caller;
import com.wildguard.conservation.dto.ActivityDto;
import com.wildguard.conservation.dto.ActivityRequest;
import com.wildguard.conservation.dto.ProgrammeDto;
import com.wildguard.conservation.dto.ProgrammeRequest;
import com.wildguard.conservation.entity.Programme;
import com.wildguard.conservation.entity.ProgrammeActivity;
import com.wildguard.conservation.entity.ProgrammeStatus;
import com.wildguard.conservation.repository.ProgrammeActivityRepository;
import com.wildguard.conservation.repository.ProgrammeRepository;

// Conservation programmes and their activities.
// The progress % is never saved. We calculate it every time from the activities.
@Service
public class ProgrammeService {

    private ProgrammeRepository programmeRepository;
    private ProgrammeActivityRepository activityRepository;
    private ActivityLog audit;

    public ProgrammeService(ProgrammeRepository programmeRepository, ProgrammeActivityRepository activityRepository,
            ActivityLog audit) {
        this.programmeRepository = programmeRepository;
        this.activityRepository = activityRepository;
        this.audit = audit;
    }

    // list of programmes, status filter is optional
    @Transactional(readOnly = true)
    public List<ProgrammeDto> list(ProgrammeStatus status) {
        List<ProgrammeDto> result = new ArrayList<>();
        for (Programme programme : programmeRepository.findAllByOrderByProgrammeCodeAsc()) {
            if (status != null && programme.getStatus() != status) {
                continue;
            }
            result.add(toDto(programme, false));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ProgrammeDto get(Long id) {
        return toDto(findProgramme(id), true);
    }

    @Transactional
    public ProgrammeDto create(ProgrammeRequest request) {
        Programme programme = new Programme();
        // first copy and check the fields, so a wrong request does not use up a code number
        copyFields(programme, request);
        // code looks like PRG-005
        programme.setProgrammeCode(String.format("PRG-%03d", programmeRepository.nextProgrammeNumber()));
        programme = programmeRepository.save(programme);

        audit.publish("PROGRAMME_CREATED", "PROGRAMME", programme.getProgrammeCode(),
                "Programme " + programme.getName() + " created with a target of " + programme.getTargetQuantity()
                        + " " + unitOf(programme) + ".");
        return toDto(programme, true);
    }

    @Transactional
    public ProgrammeDto update(Long id, ProgrammeRequest request) {
        Programme programme = findProgramme(id);
        copyFields(programme, request);
        programmeRepository.save(programme);
        audit.publish("PROGRAMME_UPDATED", "PROGRAMME", programme.getProgrammeCode(),
                "Programme " + programme.getName() + " updated.");
        return toDto(programme, true);
    }

    // Records completed work. The progress changes only because this row is saved.
    @Transactional
    public ProgrammeDto addActivity(Long id, ActivityRequest request) {
        Programme programme = findProgramme(id);
        Caller caller = Caller.current();
        if (programme.getStatus() == ProgrammeStatus.COMPLETED) {
            throw ApiException.conflict(programme.getProgrammeCode() + " is completed; reopen it before adding activities.");
        }

        ProgrammeActivity activity = new ProgrammeActivity();
        activity.setProgramme(programme);
        activity.setDescription(request.getDescription().trim());
        activity.setQuantityCompleted(request.getQuantityCompleted());
        if (request.getCompletedAt() != null) {
            activity.setCompletedAt(request.getCompletedAt());
        } else {
            activity.setCompletedAt(LocalDateTime.now());
        }
        activity.setRecordedByName(caller.displayName());
        activityRepository.save(activity);

        // if the programme was only planned, the work has started now
        if (programme.getStatus() == ProgrammeStatus.PLANNED) {
            programme.setStatus(ProgrammeStatus.ACTIVE);
            programmeRepository.save(programme);
        }

        long completed = activityRepository.sumCompleted(programme.getId());
        audit.publish("PROGRAMME_ACTIVITY", "PROGRAMME", programme.getProgrammeCode(),
                programme.getName() + ": " + request.getQuantityCompleted() + " " + unitOf(programme) + " recorded ("
                        + completed + " of " + programme.getTargetQuantity() + " complete).");
        return toDto(programme, true);
    }

    // ---------- helper methods ----------

    // progress % = completed / target * 100
    // example: 12 completed out of 20 = 60 %. We dont let it go above 100.
    public static int progressPercent(long completed, int target) {
        if (target <= 0) {
            return 0;
        }
        long percent = Math.round(completed * 100.0 / target);
        if (percent > 100) {
            percent = 100;
        }
        return (int) percent;
    }

    // overdue = due date is over but the work is not finished
    public static boolean isOverdue(Programme programme, int progress) {
        if (programme.getDueDate() == null) {
            return false;
        }
        return programme.getDueDate().isBefore(LocalDate.now())
                && programme.getStatus() != ProgrammeStatus.COMPLETED && progress < 100;
    }

    private String unitOf(Programme programme) {
        if (programme.getUnit() != null) {
            return programme.getUnit();
        }
        return "units";
    }

    private Programme findProgramme(Long id) {
        Programme programme = programmeRepository.findById(id).orElse(null);
        if (programme == null) {
            throw ApiException.notFound("Programme " + id + " was not found.");
        }
        return programme;
    }

    // copies the form values into the entity (used by create and update)
    private void copyFields(Programme programme, ProgrammeRequest request) {
        if (request.getStartDate() != null && request.getDueDate() != null
                && request.getDueDate().isBefore(request.getStartDate())) {
            throw ApiException.badRequest("The due date must not be before the start date.");
        }
        programme.setName(request.getName().trim());
        programme.setDescription(TextUtil.clean(request.getDescription()));
        programme.setTargetQuantity(request.getTargetQuantity());
        programme.setUnit(TextUtil.clean(request.getUnit()));
        programme.setStartDate(request.getStartDate());
        programme.setDueDate(request.getDueDate());
        programme.setStatus(request.getStatus());
        programme.setTeam(TextUtil.clean(request.getTeam()));

        // the person who is responsible for the programme (just the name)
        programme.setOwnerStaffName(TextUtil.clean(request.getOwnerStaffName()));
    }

    // entity -> DTO. Here the progress is calculated. detail = true also adds the activity list.
    // public because the summary service also uses it for the dashboard
    public ProgrammeDto toDto(Programme programme, boolean detail) {
        long completed = activityRepository.sumCompleted(programme.getId());
        int progress = progressPercent(completed, programme.getTargetQuantity());

        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(programme.getId());
        dto.setProgrammeCode(programme.getProgrammeCode());
        dto.setName(programme.getName());
        dto.setDescription(programme.getDescription());
        dto.setTargetQuantity(programme.getTargetQuantity());
        dto.setUnit(programme.getUnit());
        dto.setCompletedQuantity(completed);
        dto.setProgressPercent(progress);
        dto.setStartDate(programme.getStartDate());
        dto.setDueDate(programme.getDueDate());
        dto.setStatus(programme.getStatus());
        dto.setOverdue(isOverdue(programme, progress));
        dto.setOwnerStaffName(programme.getOwnerStaffName());
        dto.setTeam(programme.getTeam());

        List<ActivityDto> activities = new ArrayList<>();
        if (detail) {
            for (ProgrammeActivity activity : activityRepository.findByProgrammeIdOrderByCompletedAtDescIdDesc(programme.getId())) {
                ActivityDto a = new ActivityDto();
                a.setId(activity.getId());
                a.setDescription(activity.getDescription());
                a.setQuantityCompleted(activity.getQuantityCompleted());
                a.setCompletedAt(activity.getCompletedAt());
                a.setRecordedByName(activity.getRecordedByName());
                activities.add(a);
            }
        }
        dto.setActivities(activities);
        return dto;
    }
}
