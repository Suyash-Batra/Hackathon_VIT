package com.wildguard.conservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.client.ServiceGateway;
import com.wildguard.conservation.entity.RescueCase.ReleaseReviewStatus;
import com.wildguard.conservation.entity.RescueCase.RescueStage;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.RescueStageHistory;
import com.wildguard.conservation.entity.ResourceAllocation.AllocationTarget;
import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.TreatmentRecord;
import com.wildguard.conservation.repository.RescueCaseRepository;
import com.wildguard.conservation.repository.RescueStageHistoryRepository;
import com.wildguard.conservation.repository.ResourceAllocationRepository;
import com.wildguard.conservation.repository.TreatmentRecordRepository;
import com.wildguard.conservation.util.ErrorHandler.AppException;
import com.wildguard.conservation.util.LoginCheck.LoginUser;
import com.wildguard.conservation.util.TextUtil;

// rescue cases: open a case, change the stage, treatment notes and the release review
@Service
public class RescueService {

    private RescueCaseRepository rescueRepository;
    private RescueStageHistoryRepository historyRepository;
    private TreatmentRecordRepository treatmentRepository;
    private ResourceAllocationRepository allocationRepository;
    private ServiceGateway gateway;

    public RescueService(RescueCaseRepository rescueRepository, RescueStageHistoryRepository historyRepository,
            TreatmentRecordRepository treatmentRepository, ResourceAllocationRepository allocationRepository,
            ServiceGateway gateway) {
        this.rescueRepository = rescueRepository;
        this.historyRepository = historyRepository;
        this.treatmentRepository = treatmentRepository;
        this.allocationRepository = allocationRepository;
        this.gateway = gateway;
    }

    // list of cases, newest first. the filters are optional (null = no filter)
    public List<RescueCase> list(RescueStage stage, Boolean activeOnly, String incidentId, String animalId,
            LoginUser user) {
        List<RescueCase> result = new ArrayList<>();
        for (RescueCase rescue : rescueRepository.findAllByOrderByOpenedAtDesc()) {
            if (stage != null && rescue.getStage() != stage) {
                continue;
            }
            if (activeOnly != null && activeOnly && !rescue.isActive()) {
                continue;
            }
            if (incidentId != null && !incidentId.equals(rescue.getIncidentId())) {
                continue;
            }
            if (animalId != null && !animalId.equals(rescue.getAnimalId())) {
                continue;
            }
            result.add(prepare(rescue, user, false));
        }
        return result;
    }

    public RescueCase get(Long id, LoginUser user) {
        return prepare(find(id), user, true);
    }

    // opens a new case. form has the incident id, animal id, responsible person and summary
    @Transactional
    public RescueCase create(RescueCase form, LoginUser user) {
        RescueCase rescue = new RescueCase();
        rescue.setSummary(TextUtil.required(form.getSummary(), "Summary", 1000));

        // incident and animal belong to other services, so we ask them
        String incidentId = TextUtil.optional(form.getIncidentId(), "Incident ID", 40);
        if (incidentId != null) {
            String incidentCode = gateway.checkIncident(incidentId, user.token);
            // one incident should not have two open rescue cases
            for (RescueCase other : rescueRepository.findByIncidentId(incidentId)) {
                if (other.isActive()) {
                    throw new AppException(409, incidentCode + " already has the active rescue case "
                            + other.getRescueCode() + ".");
                }
            }
            rescue.setIncidentId(incidentId);
            rescue.setIncidentCode(incidentCode);
        }
        String animalId = TextUtil.optional(form.getAnimalId(), "Animal ID", 40);
        if (animalId != null) {
            rescue.setAnimalId(animalId);
            rescue.setAnimalCode(gateway.checkAnimal(animalId, user.token));
        }

        // if no name is typed, the person who opens the case is responsible
        String staff = TextUtil.optional(form.getAssignedStaffName(), "Responsible person", 120);
        rescue.setAssignedStaffName(staff != null ? staff : user.email);

        rescue.setRescueCode(String.format("RES-%03d", rescueRepository.nextRescueNumber())); // RES-032
        rescue.setStage(RescueStage.REPORTED);
        rescue.setReleaseReviewStatus(ReleaseReviewStatus.NONE);
        rescue.setOpenedByName(user.email);
        rescue.setOpenedAt(LocalDateTime.now());
        rescue = rescueRepository.save(rescue);

        addHistory(rescue, null, RescueStage.REPORTED, "Rescue case opened", rescue.getSummary(), user);
        if (incidentId != null) {
            gateway.markIncidentRescueInProgress(incidentId, rescue.getRescueCode(), user.token);
        }
        System.out.println("[RESCUE] " + user.email + " opened " + rescue.getRescueCode());
        return prepare(rescue, user, true);
    }

    // moves the case to the next stage. body has: stage, notes, destination, outcome
    // main rule: RELEASED is only possible when the release review is approved
    @Transactional
    public RescueCase changeStage(Long id, Map<String, String> body, LoginUser user) {
        RescueCase rescue = find(id);
        RescueStage oldStage = rescue.getStage();
        RescueStage newStage;
        try {
            newStage = RescueStage.valueOf(body.get("stage"));
        } catch (Exception ex) { // stage missing or not one of the enum values
            throw new AppException(400, "Choose a valid stage.");
        }
        String destination = TextUtil.optional(body.get("destination"), "Destination", 200);
        String outcome = TextUtil.optional(body.get("outcome"), "Outcome", 500);

        // check 1: is this move allowed? (REPORTED -> RELEASED directly is not)
        if (!oldStage.nextStages().contains(newStage)) {
            throw new AppException(409, rescue.getRescueCode() + " cannot move from " + TextUtil.label(oldStage)
                    + " to " + TextUtil.label(newStage) + ".");
        }
        // check 2: release needs an approved review
        if (newStage == RescueStage.RELEASED && rescue.getReleaseReviewStatus() != ReleaseReviewStatus.APPROVED) {
            throw new AppException(409, "Release needs an approved release review. Request the review and wait "
                    + "for a Forest Officer or Administrator to record the decision.");
        }
        // check 3: for a transfer we must know where the animal goes
        if (newStage == RescueStage.TRANSFERRED && destination == null) {
            throw new AppException(400, "Enter the destination the animal is transferred to.");
        }

        rescue.setStage(newStage);
        if (destination != null) {
            rescue.setDestination(destination);
        }
        if (outcome != null) {
            rescue.setOutcome(outcome);
        }
        // animal went back to treatment, so the old review does not count any more
        if (newStage == RescueStage.UNDER_TREATMENT && oldStage == RescueStage.REHABILITATION) {
            rescue.setReleaseReviewStatus(ReleaseReviewStatus.NONE);
        }
        if (!newStage.isActive()) {
            rescue.setClosedAt(LocalDateTime.now()); // released or transferred = case closed
        }
        rescueRepository.save(rescue);

        addHistory(rescue, oldStage, newStage, actionText(newStage),
                TextUtil.optional(body.get("notes"), "Notes", 1000), user);
        System.out.println("[RESCUE] " + rescue.getRescueCode() + ": " + oldStage + " -> " + newStage + " by " + user.email);
        return prepare(rescue, user, true);
    }

    // form has: treatment, notes
    @Transactional
    public RescueCase addTreatment(Long id, TreatmentRecord form, LoginUser user) {
        RescueCase rescue = find(id);
        if (!rescue.isActive()) {
            throw new AppException(409, rescue.getRescueCode() + " is closed; no further treatment can be recorded.");
        }
        TreatmentRecord treatment = new TreatmentRecord();
        treatment.setRescueCase(rescue);
        treatment.setTreatment(TextUtil.required(form.getTreatment(), "Treatment", 500));
        treatment.setNotes(TextUtil.optional(form.getNotes(), "Notes", 1000));
        treatment.setRecordedByName(user.email);
        treatment.setRecordedAt(LocalDateTime.now());
        treatmentRepository.save(treatment);
        return prepare(rescue, user, true);
    }

    // rescue officer asks for the release review. only in the rehabilitation stage
    @Transactional
    public RescueCase requestReleaseReview(Long id, String notes, LoginUser user) {
        RescueCase rescue = find(id);
        if (rescue.getStage() != RescueStage.REHABILITATION) {
            throw new AppException(409, "A release review can only be requested during rehabilitation.");
        }
        if (rescue.getReleaseReviewStatus() == ReleaseReviewStatus.REQUESTED) {
            throw new AppException(409, "A release review is already waiting for a decision.");
        }
        rescue.setReleaseReviewStatus(ReleaseReviewStatus.REQUESTED);
        rescue.setReleaseReviewNotes(null);
        rescue.setReleaseReviewedByName(null);
        rescue.setReleaseReviewedAt(null);
        rescueRepository.save(rescue);

        // old stage = new stage here, that is how the timeline knows it is a review entry
        addHistory(rescue, rescue.getStage(), rescue.getStage(), "Release review requested",
                TextUtil.optional(notes, "Notes", 1000), user);
        return prepare(rescue, user, true);
    }

    // forest officer or admin approves / rejects. we save who decided and when
    @Transactional
    public RescueCase decideReleaseReview(Long id, boolean approved, String notes, LoginUser user) {
        RescueCase rescue = find(id);
        if (rescue.getReleaseReviewStatus() != ReleaseReviewStatus.REQUESTED) {
            throw new AppException(409, "There is no release review waiting for a decision.");
        }
        notes = TextUtil.optional(notes, "Notes", 1000);
        rescue.setReleaseReviewStatus(approved ? ReleaseReviewStatus.APPROVED : ReleaseReviewStatus.REJECTED);
        rescue.setReleaseReviewNotes(notes);
        rescue.setReleaseReviewedByName(user.email);
        rescue.setReleaseReviewedAt(LocalDateTime.now());
        rescueRepository.save(rescue);

        String word = approved ? "approved" : "rejected";
        addHistory(rescue, rescue.getStage(), rescue.getStage(), "Release review " + word, notes, user);
        System.out.println("[RESCUE] " + user.email + " " + word + " the release of " + rescue.getRescueCode());
        return prepare(rescue, user, true);
    }

    // ---------- helper methods ----------

    // also used by ResourceService
    public RescueCase find(Long id) {
        RescueCase rescue = rescueRepository.findById(id).orElse(null);
        if (rescue == null) {
            throw new AppException(404, "Rescue case " + id + " was not found.");
        }
        return rescue;
    }

    private String actionText(RescueStage stage) {
        if (stage == RescueStage.RESCUED) {
            return "Animal rescued";
        } else if (stage == RescueStage.UNDER_TREATMENT) {
            return "Treatment started";
        } else if (stage == RescueStage.REHABILITATION) {
            return "Rehabilitation started";
        } else if (stage == RescueStage.RELEASED) {
            return "Animal released";
        }
        return "Animal transferred";
    }

    private void addHistory(RescueCase rescue, RescueStage oldStage, RescueStage newStage, String action,
            String notes, LoginUser user) {
        RescueStageHistory history = new RescueStageHistory();
        history.setRescueCase(rescue);
        history.setOldStage(oldStage);
        history.setNewStage(newStage);
        history.setAction(action);
        history.setNotes(notes);
        history.setChangedByName(user.email);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    // timeline = stage history + treatment notes in one list, sorted by time
    private List<TimelineEntry> buildTimeline(Long rescueId) {
        List<TimelineEntry> entries = new ArrayList<>();
        for (RescueStageHistory history : historyRepository.findByRescueCaseIdOrderByChangedAtAscIdAsc(rescueId)) {
            TimelineEntry entry = new TimelineEntry();
            entry.at = history.getChangedAt();
            entry.title = history.getAction();
            entry.person = history.getChangedByName();
            entry.notes = history.getNotes();
            if (history.getOldStage() == history.getNewStage()) {
                entry.kind = "REVIEW";
            } else {
                entry.kind = "STAGE";
                entry.stage = history.getNewStage();
            }
            entries.add(entry);
        }
        for (TreatmentRecord treatment : treatmentRepository.findByRescueCaseIdOrderByRecordedAtAscIdAsc(rescueId)) {
            TimelineEntry entry = new TimelineEntry();
            entry.at = treatment.getRecordedAt();
            entry.kind = "TREATMENT";
            entry.title = treatment.getTreatment();
            entry.person = treatment.getRecordedByName();
            entry.notes = treatment.getNotes();
            entries.add(entry);
        }
        entries.sort((a, b) -> a.at.compareTo(b.at)); // oldest first
        return entries;
    }

    // fills the extra (not saved) fields of the case before it goes to the page:
    // which stages / buttons this user can use, and for the detail page the timeline too
    private RescueCase prepare(RescueCase rescue, LoginUser user, boolean detail) {
        ReleaseReviewStatus review = rescue.getReleaseReviewStatus();
        boolean rescueOfficer = user.is("ADMIN", "RESCUE_OFFICER");
        boolean forestOfficer = user.is("ADMIN", "FOREST_OFFICER");

        List<RescueStage> allowed = new ArrayList<>();
        for (RescueStage next : rescue.getStage().nextStages()) {
            // "Released" is not offered until the review is approved
            if (next == RescueStage.RELEASED && review != ReleaseReviewStatus.APPROVED) {
                continue;
            }
            allowed.add(next);
        }
        rescue.setAllowedNextStages(allowed);
        rescue.setCanAddTreatment(rescue.isActive() && rescueOfficer);
        rescue.setCanRequestReview(rescueOfficer && rescue.getStage() == RescueStage.REHABILITATION
                && review != ReleaseReviewStatus.REQUESTED && review != ReleaseReviewStatus.APPROVED);
        rescue.setCanDecideReview(forestOfficer && review == ReleaseReviewStatus.REQUESTED);

        if (detail) {
            rescue.setTimeline(buildTimeline(rescue.getId()));
            // resources that are with this case right now
            List<ResourceAllocation> inUse = new ArrayList<>();
            for (ResourceAllocation allocation : allocationRepository.findByTargetTypeAndTargetIdOrderByAllocatedAtDesc(
                    AllocationTarget.RESCUE_CASE, String.valueOf(rescue.getId()))) {
                if (allocation.isActive()) {
                    inUse.add(allocation);
                }
            }
            rescue.setAllocations(inUse);
        }
        return rescue;
    }

    // one line of the Rescue Journey Timeline shown on the rescue page.
    // kind is STAGE, TREATMENT or REVIEW
    public static class TimelineEntry {
        public LocalDateTime at;
        public String kind;
        public String title;
        public RescueStage stage;
        public String person;
        public String notes;
    }
}
