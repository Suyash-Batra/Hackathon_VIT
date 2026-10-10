package com.wildguard.conservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.entity.Programme.ProgrammeStatus;
import com.wildguard.conservation.entity.Programme;
import com.wildguard.conservation.entity.ProgrammeActivity;
import com.wildguard.conservation.repository.ProgrammeActivityRepository;
import com.wildguard.conservation.repository.ProgrammeRepository;
import com.wildguard.conservation.util.ErrorHandler.AppException;
import com.wildguard.conservation.util.LoginCheck.LoginUser;
import com.wildguard.conservation.util.TextUtil;

// conservation programmes and their progress.
//   progress % = completed quantity / target quantity * 100
// The % is not stored anywhere, it is calculated from the activities every time.
@Service
public class ProgrammeService {

    private ProgrammeRepository programmeRepository;
    private ProgrammeActivityRepository activityRepository;

    public ProgrammeService(ProgrammeRepository programmeRepository, ProgrammeActivityRepository activityRepository) {
        this.programmeRepository = programmeRepository;
        this.activityRepository = activityRepository;
    }

    public List<Programme> list(ProgrammeStatus status) {
        List<Programme> result = new ArrayList<>();
        for (Programme programme : programmeRepository.findAllByOrderByProgrammeCodeAsc()) {
            if (status == null || programme.getStatus() == status) {
                result.add(prepare(programme, false));
            }
        }
        return result;
    }

    public Programme get(Long id) {
        return prepare(find(id), true);
    }

    @Transactional
    public Programme create(Programme form) {
        Programme programme = new Programme();
        copyFields(programme, form); // check first, so a wrong form does not use up a code number
        programme.setProgrammeCode(String.format("PRG-%03d", programmeRepository.nextProgrammeNumber())); // PRG-005
        programme = programmeRepository.save(programme);
        System.out.println("[PROGRAMME] " + programme.getProgrammeCode() + " " + programme.getName() + " created");
        return prepare(programme, true);
    }

    @Transactional
    public Programme update(Long id, Programme form) {
        Programme programme = find(id);
        copyFields(programme, form);
        programmeRepository.save(programme);
        return prepare(programme, true);
    }

    // records completed work, for example "3 water sources restored"
    @Transactional
    public Programme addActivity(Long id, ProgrammeActivity form, LoginUser user) {
        Programme programme = find(id);
        if (programme.getStatus() == ProgrammeStatus.COMPLETED) {
            throw new AppException(409, programme.getProgrammeCode() + " is completed; reopen it before adding activities.");
        }
        if (form.getQuantityCompleted() == null || form.getQuantityCompleted() < 1) {
            throw new AppException(400, "Quantity completed must be at least 1.");
        }
        ProgrammeActivity activity = new ProgrammeActivity();
        activity.setProgramme(programme);
        activity.setDescription(TextUtil.required(form.getDescription(), "Description", 500));
        activity.setQuantityCompleted(form.getQuantityCompleted());
        activity.setCompletedAt(form.getCompletedAt() != null ? form.getCompletedAt() : LocalDateTime.now());
        activity.setRecordedByName(user.email);
        activityRepository.save(activity);

        // work has started, so a planned programme becomes active
        if (programme.getStatus() == ProgrammeStatus.PLANNED) {
            programme.setStatus(ProgrammeStatus.ACTIVE);
            programmeRepository.save(programme);
        }
        return prepare(programme, true);
    }

    // the formula. 12 of 20 -> 60. never more than 100
    public static int progressPercent(long completed, int target) {
        if (target <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(completed * 100.0 / target));
    }

    // ---------- helper methods ----------

    private Programme find(Long id) {
        Programme programme = programmeRepository.findById(id).orElse(null);
        if (programme == null) {
            throw new AppException(404, "Programme " + id + " was not found.");
        }
        return programme;
    }

    // copies the form into the entity (used by create and update) and checks the values
    private void copyFields(Programme programme, Programme form) {
        if (form.getTargetQuantity() == null || form.getTargetQuantity() < 1) {
            throw new AppException(400, "Target quantity must be at least 1.");
        }
        if (form.getStartDate() != null && form.getDueDate() != null && form.getDueDate().isBefore(form.getStartDate())) {
            throw new AppException(400, "The due date must not be before the start date.");
        }
        programme.setName(TextUtil.required(form.getName(), "Name", 150));
        programme.setDescription(TextUtil.optional(form.getDescription(), "Description", 1000));
        programme.setTargetQuantity(form.getTargetQuantity());
        programme.setUnit(TextUtil.optional(form.getUnit(), "Unit", 40));
        programme.setStartDate(form.getStartDate());
        programme.setDueDate(form.getDueDate());
        programme.setStatus(form.getStatus() != null ? form.getStatus() : ProgrammeStatus.PLANNED);
        programme.setOwnerStaffName(TextUtil.optional(form.getOwnerStaffName(), "Owner", 120));
        programme.setTeam(TextUtil.optional(form.getTeam(), "Team", 60));
    }

    // calculates the progress and puts it in the (not saved) fields. also used by the summary
    public Programme prepare(Programme programme, boolean detail) {
        long completed = activityRepository.sumCompleted(programme.getId());
        int progress = progressPercent(completed, programme.getTargetQuantity());
        programme.setCompletedQuantity(completed);
        programme.setProgressPercent(progress);
        // overdue = due date is over but the work is not finished
        programme.setOverdue(programme.getDueDate() != null && programme.getDueDate().isBefore(LocalDate.now())
                && programme.getStatus() != ProgrammeStatus.COMPLETED && progress < 100);
        if (detail) {
            programme.setActivities(activityRepository.findByProgrammeIdOrderByCompletedAtDescIdDesc(programme.getId()));
        }
        return programme;
    }
}
