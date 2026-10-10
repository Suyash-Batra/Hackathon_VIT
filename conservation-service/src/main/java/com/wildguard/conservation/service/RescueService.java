package com.wildguard.conservation.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.service.ActivityLog;
import com.wildguard.conservation.error.ApiException;
import com.wildguard.conservation.security.Caller;
import com.wildguard.conservation.security.Role;
import com.wildguard.conservation.client.ServiceGateway;
import com.wildguard.conservation.dto.AllocationDto;
import com.wildguard.conservation.dto.RescueDto;
import com.wildguard.conservation.dto.RescueRequest;
import com.wildguard.conservation.dto.ReviewDecisionRequest;
import com.wildguard.conservation.dto.StageChangeRequest;
import com.wildguard.conservation.dto.TimelineEntry;
import com.wildguard.conservation.dto.TreatmentRequest;
import com.wildguard.conservation.entity.AllocationTarget;
import com.wildguard.conservation.entity.ReleaseReviewStatus;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.RescueStage;
import com.wildguard.conservation.entity.RescueStageHistory;
import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.TreatmentRecord;
import com.wildguard.conservation.repository.RescueCaseRepository;
import com.wildguard.conservation.repository.RescueStageHistoryRepository;
import com.wildguard.conservation.repository.ResourceAllocationRepository;
import com.wildguard.conservation.repository.TreatmentRecordRepository;

// All the logic for rescue cases is here: open a case, change the stage,
// add treatment notes and the release review.
@Service
public class RescueService {

    private RescueCaseRepository rescueRepository;
    private RescueStageHistoryRepository historyRepository;
    private TreatmentRecordRepository treatmentRepository;
    private ResourceAllocationRepository allocationRepository;
    private ServiceGateway gateway;
    private ActivityLog audit;

    // Spring gives us these objects (constructor injection)
    public RescueService(RescueCaseRepository rescueRepository, RescueStageHistoryRepository historyRepository,
            TreatmentRecordRepository treatmentRepository, ResourceAllocationRepository allocationRepository,
            ServiceGateway gateway, ActivityLog audit) {
        this.rescueRepository = rescueRepository;
        this.historyRepository = historyRepository;
        this.treatmentRepository = treatmentRepository;
        this.allocationRepository = allocationRepository;
        this.gateway = gateway;
        this.audit = audit;
    }

    // list of rescue cases. all 4 filters are optional (null = dont filter)
    @Transactional(readOnly = true)
    public List<RescueDto> list(RescueStage stage, Boolean activeOnly, String incidentId, String animalId) {
        Caller caller = Caller.current();
        List<RescueDto> result = new ArrayList<>();
        for (RescueCase rescue : rescueRepository.findAllByOrderByOpenedAtDesc()) {
            if (stage != null && rescue.getStage() != stage) {
                continue;
            }
            if (activeOnly != null && activeOnly && !rescue.getStage().isActive()) {
                continue;
            }
            if (incidentId != null && !incidentId.equals(rescue.getIncidentId())) {
                continue;
            }
            if (animalId != null && !animalId.equals(rescue.getAnimalId())) {
                continue;
            }
            result.add(toDto(rescue, caller, false));
        }
        return result;
    }

    // one rescue case with its timeline
    @Transactional(readOnly = true)
    public RescueDto get(Long id) {
        return toDto(findCase(id), Caller.current(), true);
    }

    // Opens a new rescue case.
    // The incident and the animal belong to other services so we check them with REST calls.
    @Transactional
    public RescueDto create(RescueRequest request) {
        Caller caller = Caller.current();
        RescueCase rescue = new RescueCase();
        String incidentId = TextUtil.clean(request.getIncidentId());
        String animalId = TextUtil.clean(request.getAnimalId());

        if (incidentId != null) {
            String incidentCode = gateway.checkIncident(incidentId);

            // one incident should not have two open rescue cases
            for (RescueCase other : rescueRepository.findByIncidentId(incidentId)) {
                if (other.getStage().isActive()) {
                    throw ApiException.conflict(incidentCode + " already has the active rescue case "
                            + other.getRescueCode() + ".");
                }
            }
            rescue.setIncidentId(incidentId);
            rescue.setIncidentCode(incidentCode);
        }

        if (animalId != null) {
            rescue.setAnimalId(animalId);
            rescue.setAnimalCode(gateway.checkAnimal(animalId));
        }

        // who is responsible for the case. if nothing is typed it is the person who opens it
        String staffName = TextUtil.clean(request.getAssignedStaffName());
        if (staffName == null) {
            staffName = caller.displayName();
        }
        rescue.setAssignedStaffName(staffName);

        // code looks like RES-032
        rescue.setRescueCode(String.format("RES-%03d", rescueRepository.nextRescueNumber()));
        rescue.setSummary(request.getSummary().trim());
        rescue.setStage(RescueStage.REPORTED);
        rescue.setReleaseReviewStatus(ReleaseReviewStatus.NONE);
        rescue.setOpenedByName(caller.displayName());
        rescue.setOpenedAt(LocalDateTime.now());
        rescue = rescueRepository.save(rescue);

        // first entry of the timeline
        addHistory(rescue, null, RescueStage.REPORTED, "Rescue case opened", rescue.getSummary(), caller);

        // tell operations-service that the incident now has a rescue running
        if (rescue.getIncidentId() != null) {
            gateway.markIncidentRescueInProgress(rescue.getIncidentId(), rescue.getRescueCode());
        }

        String message = "Rescue case " + rescue.getRescueCode() + " opened";
        if (rescue.getIncidentCode() != null) {
            message = message + " for " + rescue.getIncidentCode();
        }
        if (rescue.getAnimalCode() != null) {
            message = message + " and animal " + rescue.getAnimalCode();
        }
        audit.publish("RESCUE_OPENED", "RESCUE", rescue.getRescueCode(), message + ".");

        return toDto(rescue, caller, true);
    }

    // Moves the case to the next stage.
    // Important rule: the animal can be RELEASED only if the release review was approved.
    // So the system never decides by itself that an animal is fit for release.
    @Transactional
    public RescueDto changeStage(Long id, StageChangeRequest request) {
        RescueCase rescue = findCase(id);
        Caller caller = Caller.current();
        RescueStage oldStage = rescue.getStage();
        RescueStage newStage = request.getStage();

        // check 1: is this move allowed? (for example REPORTED -> RELEASED is not allowed)
        if (!oldStage.nextStages().contains(newStage)) {
            throw ApiException.conflict(rescue.getRescueCode() + " cannot move from " + TextUtil.label(oldStage)
                    + " to " + TextUtil.label(newStage) + ".");
        }
        // check 2: release needs an approved review
        if (newStage == RescueStage.RELEASED && rescue.getReleaseReviewStatus() != ReleaseReviewStatus.APPROVED) {
            throw ApiException.conflict("Release needs an approved release review. Request the review and wait "
                    + "for a Forest Officer or Administrator to record the decision.");
        }
        // check 3: for a transfer we must know where the animal is going
        if (newStage == RescueStage.TRANSFERRED && TextUtil.clean(request.getDestination()) == null) {
            throw ApiException.badRequest("Enter the destination the animal is transferred to.");
        }

        rescue.setStage(newStage);
        if (TextUtil.clean(request.getDestination()) != null) {
            rescue.setDestination(request.getDestination().trim());
        }
        if (TextUtil.clean(request.getOutcome()) != null) {
            rescue.setOutcome(request.getOutcome().trim());
        }
        // if the animal goes back to treatment the old review is not valid anymore
        if (newStage == RescueStage.UNDER_TREATMENT && oldStage == RescueStage.REHABILITATION) {
            rescue.setReleaseReviewStatus(ReleaseReviewStatus.NONE);
        }
        // released or transferred = case is closed
        if (!newStage.isActive()) {
            rescue.setClosedAt(LocalDateTime.now());
        }
        rescueRepository.save(rescue);

        addHistory(rescue, oldStage, newStage, actionText(newStage), TextUtil.clean(request.getNotes()), caller);
        audit.publish("RESCUE_STAGE_CHANGED", "RESCUE", rescue.getRescueCode(),
                rescue.getRescueCode() + ": " + TextUtil.label(oldStage) + " -> " + TextUtil.label(newStage)
                        + " by " + caller.displayName() + ".");
        return toDto(rescue, caller, true);
    }

    // rescue officer adds a treatment note
    @Transactional
    public RescueDto addTreatment(Long id, TreatmentRequest request) {
        RescueCase rescue = findCase(id);
        Caller caller = Caller.current();
        if (!rescue.getStage().isActive()) {
            throw ApiException.conflict(rescue.getRescueCode() + " is closed; no further treatment can be recorded.");
        }
        TreatmentRecord treatment = new TreatmentRecord();
        treatment.setRescueCase(rescue);
        treatment.setTreatment(request.getTreatment().trim());
        treatment.setNotes(TextUtil.clean(request.getNotes()));
        treatment.setRecordedByName(caller.displayName());
        treatment.setRecordedAt(LocalDateTime.now());
        treatmentRepository.save(treatment);

        audit.publish("TREATMENT_RECORDED", "RESCUE", rescue.getRescueCode(),
                caller.displayName() + " recorded a treatment update for " + rescue.getRescueCode() + ".");
        return toDto(rescue, caller, true);
    }

    // rescue officer asks for a release review. only possible in the rehabilitation stage
    @Transactional
    public RescueDto requestReleaseReview(Long id, String notes) {
        RescueCase rescue = findCase(id);
        Caller caller = Caller.current();
        if (rescue.getStage() != RescueStage.REHABILITATION) {
            throw ApiException.conflict("A release review can only be requested during rehabilitation.");
        }
        if (rescue.getReleaseReviewStatus() == ReleaseReviewStatus.REQUESTED) {
            throw ApiException.conflict("A release review is already waiting for a decision.");
        }
        rescue.setReleaseReviewStatus(ReleaseReviewStatus.REQUESTED);
        rescue.setReleaseReviewNotes(null);
        rescue.setReleaseReviewedByName(null);
        rescue.setReleaseReviewedAt(null);
        rescueRepository.save(rescue);

        // old stage and new stage are the same here, that is how we know it is a review entry
        addHistory(rescue, rescue.getStage(), rescue.getStage(), "Release review requested", TextUtil.clean(notes), caller);
        audit.publish("RELEASE_REVIEW_REQUESTED", "RESCUE", rescue.getRescueCode(),
                caller.displayName() + " requested a release review for " + rescue.getRescueCode() + ".");
        return toDto(rescue, caller, true);
    }

    // officer or admin approves or rejects the release. we save who decided and when
    @Transactional
    public RescueDto decideReleaseReview(Long id, ReviewDecisionRequest request) {
        RescueCase rescue = findCase(id);
        Caller caller = Caller.current();
        if (rescue.getReleaseReviewStatus() != ReleaseReviewStatus.REQUESTED) {
            throw ApiException.conflict("There is no release review waiting for a decision.");
        }
        boolean approved = request.getApproved();
        if (approved) {
            rescue.setReleaseReviewStatus(ReleaseReviewStatus.APPROVED);
        } else {
            rescue.setReleaseReviewStatus(ReleaseReviewStatus.REJECTED);
        }
        rescue.setReleaseReviewNotes(TextUtil.clean(request.getNotes()));
        rescue.setReleaseReviewedByName(caller.displayName());
        rescue.setReleaseReviewedAt(LocalDateTime.now());
        rescueRepository.save(rescue);

        String word = approved ? "approved" : "rejected";
        addHistory(rescue, rescue.getStage(), rescue.getStage(), "Release review " + word,
                TextUtil.clean(request.getNotes()), caller);
        audit.publish(approved ? "RELEASE_APPROVED" : "RELEASE_REJECTED", "RESCUE", rescue.getRescueCode(),
                caller.displayName() + " " + word + " the release of " + rescue.getRescueCode() + ".");
        return toDto(rescue, caller, true);
    }

    // ---------- helper methods ----------

    // finds the case or sends a 404 error. also used by ResourceService
    public RescueCase findCase(Long id) {
        RescueCase rescue = rescueRepository.findById(id).orElse(null);
        if (rescue == null) {
            throw ApiException.notFound("Rescue case " + id + " was not found.");
        }
        return rescue;
    }

    // text shown in the timeline for each stage
    private String actionText(RescueStage stage) {
        if (stage == RescueStage.RESCUED) {
            return "Animal rescued";
        } else if (stage == RescueStage.UNDER_TREATMENT) {
            return "Treatment started";
        } else if (stage == RescueStage.REHABILITATION) {
            return "Rehabilitation started";
        } else if (stage == RescueStage.RELEASED) {
            return "Animal released";
        } else if (stage == RescueStage.TRANSFERRED) {
            return "Animal transferred";
        }
        return "Rescue case opened";
    }

    // saves one row in rescue_stage_history
    private void addHistory(RescueCase rescue, RescueStage oldStage, RescueStage newStage, String action,
            String notes, Caller caller) {
        RescueStageHistory history = new RescueStageHistory();
        history.setRescueCase(rescue);
        history.setOldStage(oldStage);
        history.setNewStage(newStage);
        history.setAction(action);
        history.setNotes(notes);
        history.setChangedByName(caller.displayName());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    // Builds the Rescue Journey Timeline.
    // We take the stage history and the treatment notes, put them in one list and sort by time.
    private List<TimelineEntry> buildTimeline(Long rescueId) {
        List<TimelineEntry> entries = new ArrayList<>();

        for (RescueStageHistory history : historyRepository.findByRescueCaseIdOrderByChangedAtAscIdAsc(rescueId)) {
            boolean isReview = history.getOldStage() == history.getNewStage();
            TimelineEntry entry = new TimelineEntry();
            entry.setAt(history.getChangedAt());
            entry.setTitle(history.getAction());
            entry.setPerson(history.getChangedByName());
            entry.setNotes(history.getNotes());
            if (isReview) {
                entry.setKind("REVIEW");
            } else {
                entry.setKind("STAGE");
                entry.setStage(history.getNewStage());
            }
            entries.add(entry);
        }

        for (TreatmentRecord treatment : treatmentRepository.findByRescueCaseIdOrderByRecordedAtAscIdAsc(rescueId)) {
            TimelineEntry entry = new TimelineEntry();
            entry.setAt(treatment.getRecordedAt());
            entry.setKind("TREATMENT");
            entry.setTitle(treatment.getTreatment());
            entry.setPerson(treatment.getRecordedByName());
            entry.setNotes(treatment.getNotes());
            entries.add(entry);
        }

        // simple sort by time, oldest first (the list is small so a simple sort is fine)
        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                if (entries.get(j).getAt().isBefore(entries.get(i).getAt())) {
                    TimelineEntry temp = entries.get(i);
                    entries.set(i, entries.get(j));
                    entries.set(j, temp);
                }
            }
        }
        return entries;
    }

    // Copies the entity into the DTO that goes out as JSON.
    // detail = true means we also add the timeline and the allocated resources.
    private RescueDto toDto(RescueCase rescue, Caller caller, boolean detail) {
        boolean active = rescue.getStage().isActive();
        boolean isRescueOfficerOrAdmin = caller.hasRole(Role.ADMIN, Role.RESCUE_OFFICER);
        boolean isOfficerOrAdmin = caller.hasRole(Role.ADMIN, Role.FOREST_OFFICER);
        boolean canChangeStage = caller.hasRole(Role.ADMIN, Role.RESCUE_OFFICER, Role.FOREST_OFFICER);
        ReleaseReviewStatus review = rescue.getReleaseReviewStatus();

        RescueDto dto = new RescueDto();
        dto.setId(rescue.getId());
        dto.setRescueCode(rescue.getRescueCode());
        dto.setIncidentId(rescue.getIncidentId());
        dto.setIncidentCode(rescue.getIncidentCode());
        dto.setAnimalId(rescue.getAnimalId());
        dto.setAnimalCode(rescue.getAnimalCode());
        dto.setAssignedStaffName(rescue.getAssignedStaffName());
        dto.setStage(rescue.getStage());
        dto.setActive(active);
        dto.setSummary(rescue.getSummary());
        dto.setDestination(rescue.getDestination());
        dto.setOutcome(rescue.getOutcome());
        dto.setReleaseReviewStatus(review);
        dto.setReleaseReviewNotes(rescue.getReleaseReviewNotes());
        dto.setReleaseReviewedByName(rescue.getReleaseReviewedByName());
        dto.setReleaseReviewedAt(rescue.getReleaseReviewedAt());
        dto.setOpenedByName(rescue.getOpenedByName());
        dto.setOpenedAt(rescue.getOpenedAt());
        dto.setClosedAt(rescue.getClosedAt());

        // how many days the case is open (until today, or until it was closed)
        LocalDateTime end = LocalDateTime.now();
        if (rescue.getClosedAt() != null) {
            end = rescue.getClosedAt();
        }
        dto.setDaysOpen(ChronoUnit.DAYS.between(rescue.getOpenedAt(), end));

        // which stages can this user move the case to
        List<RescueStage> allowed = new ArrayList<>();
        if (canChangeStage) {
            for (RescueStage next : rescue.getStage().nextStages()) {
                // dont offer "Released" until the review is approved
                if (next == RescueStage.RELEASED && review != ReleaseReviewStatus.APPROVED) {
                    continue;
                }
                allowed.add(next);
            }
        }
        dto.setAllowedNextStages(allowed);

        // which buttons the page should show
        dto.setCanAddTreatment(active && isRescueOfficerOrAdmin);
        dto.setCanRequestReview(isRescueOfficerOrAdmin && rescue.getStage() == RescueStage.REHABILITATION
                && review != ReleaseReviewStatus.REQUESTED && review != ReleaseReviewStatus.APPROVED);
        dto.setCanDecideReview(isOfficerOrAdmin && review == ReleaseReviewStatus.REQUESTED);

        List<TimelineEntry> timeline = new ArrayList<>();
        List<AllocationDto> allocations = new ArrayList<>();
        if (detail) {
            timeline = buildTimeline(rescue.getId());
            List<ResourceAllocation> all = allocationRepository
                    .findByTargetTypeAndTargetIdOrderByAllocatedAtDesc(AllocationTarget.RESCUE_CASE, String.valueOf(rescue.getId()));
            for (ResourceAllocation allocation : all) {
                if (allocation.getReleasedAt() == null) {
                    allocations.add(ResourceService.toAllocationDto(allocation));
                }
            }
        }
        dto.setTimeline(timeline);
        dto.setAllocations(allocations);
        return dto;
    }
}
