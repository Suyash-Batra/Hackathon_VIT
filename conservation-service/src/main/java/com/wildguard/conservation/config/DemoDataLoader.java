package com.wildguard.conservation.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.entity.Programme.ProgrammeStatus;
import com.wildguard.conservation.entity.Programme;
import com.wildguard.conservation.entity.ProgrammeActivity;
import com.wildguard.conservation.entity.RescueCase.ReleaseReviewStatus;
import com.wildguard.conservation.entity.RescueCase.RescueStage;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.RescueStageHistory;
import com.wildguard.conservation.entity.ResourceAllocation.AllocationTarget;
import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.ResourceItem.ResourceCategory;
import com.wildguard.conservation.entity.ResourceItem.ResourceStatus;
import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.entity.TreatmentRecord;
import com.wildguard.conservation.repository.ProgrammeActivityRepository;
import com.wildguard.conservation.repository.ProgrammeRepository;
import com.wildguard.conservation.repository.RescueCaseRepository;
import com.wildguard.conservation.repository.RescueStageHistoryRepository;
import com.wildguard.conservation.repository.ResourceAllocationRepository;
import com.wildguard.conservation.repository.ResourceItemRepository;
import com.wildguard.conservation.repository.TreatmentRecordRepository;

// Puts some demo data (fictional) in the tables so the pages are not empty in the demo.
// It runs once when the service starts and does nothing if there is data already.
// Switch it off with wildguard.load-demo-data=false in application.properties.
@Component
public class DemoDataLoader implements CommandLineRunner {

    private RescueCaseRepository rescueRepository;
    private RescueStageHistoryRepository historyRepository;
    private TreatmentRecordRepository treatmentRepository;
    private ResourceItemRepository resourceRepository;
    private ResourceAllocationRepository allocationRepository;
    private ProgrammeRepository programmeRepository;
    private ProgrammeActivityRepository activityRepository;

    @Value("${wildguard.load-demo-data}")
    private boolean loadDemoData;

    public DemoDataLoader(RescueCaseRepository rescueRepository, RescueStageHistoryRepository historyRepository,
            TreatmentRecordRepository treatmentRepository, ResourceItemRepository resourceRepository,
            ResourceAllocationRepository allocationRepository, ProgrammeRepository programmeRepository,
            ProgrammeActivityRepository activityRepository) {
        this.rescueRepository = rescueRepository;
        this.historyRepository = historyRepository;
        this.treatmentRepository = treatmentRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.programmeRepository = programmeRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!loadDemoData) {
            return;
        }
        // only when all three main tables are empty
        if (rescueRepository.count() > 0 || resourceRepository.count() > 0 || programmeRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String vet = "Dr. Kavya Iyer";
        String vet2 = "Dr. Sameer Khan";
        String officer = "Meera Nair";

        // ---------- rescue cases ----------

        // RES-031: injured leopard, still under treatment
        RescueCase leopard = rescue("RES-031", "INC-109", "AN-1045", vet, RescueStage.UNDER_TREATMENT,
                "Injured leopard with a wire snare wound on the left foreleg.", now.minusDays(4));
        rescueRepository.save(leopard);
        history(leopard, null, RescueStage.REPORTED, "Animal injury reported",
                "Reported by a field ranger as INC-109.", "Arjun Rao", now.minusDays(4));
        history(leopard, RescueStage.REPORTED, RescueStage.RESCUED, "Rescue team assigned",
                "Animal sedated and moved to the rescue unit in VH-04.", vet, now.minusDays(4).plusHours(3));
        history(leopard, RescueStage.RESCUED, RescueStage.UNDER_TREATMENT, "Veterinary assessment recorded",
                "Deep laceration, no fracture. Treatment plan started.", vet, now.minusDays(3));
        treatment(leopard, "Snare wire removed; wound cleaned and sutured.", "Sedation uneventful. Start antibiotics.",
                vet, now.minusDays(3).plusMinutes(30));
        treatment(leopard, "Antibiotics and pain relief, day 2.", "Eating well. Slight swelling around the sutures.",
                vet, now.minusDays(2));

        // RES-028: pangolin in rehabilitation, release review is waiting for an officer
        RescueCase pangolin = rescue("RES-028", null, "AN-1062", vet2, RescueStage.REHABILITATION,
                "Pangolin found dehydrated near the ridge trail.", now.minusDays(9));
        pangolin.setReleaseReviewStatus(ReleaseReviewStatus.REQUESTED);
        rescueRepository.save(pangolin);
        history(pangolin, null, RescueStage.REPORTED, "Rescue case opened",
                "Pangolin found dehydrated near the ridge trail.", vet, now.minusDays(9));
        history(pangolin, RescueStage.REPORTED, RescueStage.RESCUED, "Animal rescued", null, vet2,
                now.minusDays(9).plusHours(3));
        history(pangolin, RescueStage.RESCUED, RescueStage.UNDER_TREATMENT, "Treatment started",
                "Fluids and tick treatment.", vet2, now.minusDays(8));
        history(pangolin, RescueStage.UNDER_TREATMENT, RescueStage.REHABILITATION, "Rehabilitation started",
                "Feeding on termites without help.", vet2, now.minusDays(4));
        history(pangolin, RescueStage.REHABILITATION, RescueStage.REHABILITATION, "Release review requested",
                "Weight back to normal; requesting release near Zone D.", vet, now.minusDays(1));
        treatment(pangolin, "Subcutaneous fluids and tick removal.", null, vet2, now.minusDays(8).plusHours(1));

        // RES-029: leopard that was released after an approved review
        RescueCase released = rescue("RES-029", null, "AN-1073", vet, RescueStage.RELEASED,
                "Leopard with a porcupine quill injury on the right paw.", now.minusDays(20));
        released.setOutcome("Recovered fully and released in Zone B.");
        released.setReleaseReviewStatus(ReleaseReviewStatus.APPROVED);
        released.setReleaseReviewNotes("Gait and hunting behaviour normal for 5 days.");
        released.setReleaseReviewedByName(officer);
        released.setReleaseReviewedAt(now.minusDays(7));
        released.setClosedAt(now.minusDays(6));
        rescueRepository.save(released);
        history(released, null, RescueStage.REPORTED, "Rescue case opened",
                "Leopard limping on camera trap footage.", vet, now.minusDays(20));
        history(released, RescueStage.REPORTED, RescueStage.RESCUED, "Animal rescued", null, vet, now.minusDays(19));
        history(released, RescueStage.RESCUED, RescueStage.UNDER_TREATMENT, "Treatment started",
                "Quills removed under sedation.", vet, now.minusDays(19).plusHours(4));
        history(released, RescueStage.UNDER_TREATMENT, RescueStage.REHABILITATION, "Rehabilitation started", null,
                vet, now.minusDays(13));
        history(released, RescueStage.REHABILITATION, RescueStage.REHABILITATION, "Release review requested", null,
                vet, now.minusDays(8));
        history(released, RescueStage.REHABILITATION, RescueStage.REHABILITATION, "Release review approved",
                "Gait and hunting behaviour normal for 5 days.", officer, now.minusDays(7));
        history(released, RescueStage.REHABILITATION, RescueStage.RELEASED, "Animal released",
                "Released at dawn near Deep Forest Spring.", vet, now.minusDays(6));
        treatment(released, "Seven porcupine quills removed from the right paw.",
                "Wound flushed; no bone involvement.", vet, now.minusDays(19).plusHours(5));

        // RES-030: bear cub that was transferred to another centre
        RescueCase cub = rescue("RES-030", null, null, vet2, RescueStage.TRANSFERRED,
                "Orphaned sloth bear cub brought in by villagers.", now.minusDays(8));
        cub.setDestination("State Wildlife Rescue Centre");
        cub.setOutcome("Needs long-term hand rearing.");
        cub.setClosedAt(now.minusDays(5));
        rescueRepository.save(cub);
        history(cub, null, RescueStage.REPORTED, "Rescue case opened",
                "Orphaned sloth bear cub brought in by villagers.", vet, now.minusDays(8));
        history(cub, RescueStage.REPORTED, RescueStage.RESCUED, "Animal rescued", null, vet2,
                now.minusDays(8).plusHours(2));
        history(cub, RescueStage.RESCUED, RescueStage.TRANSFERRED, "Animal transferred",
                "Transferred to the State Wildlife Rescue Centre.", vet2, now.minusDays(5));

        // ---------- vehicles and equipment ----------

        ResourceItem van = resource("VH-04", ResourceCategory.VEHICLE, "Rescue van", ResourceStatus.ALLOCATED,
                "Fitted with a transport cage.");
        resource("VH-05", ResourceCategory.VEHICLE, "Patrol jeep", ResourceStatus.AVAILABLE, null);
        resource("VH-06", ResourceCategory.VEHICLE, "Pickup truck", ResourceStatus.MAINTENANCE,
                "Brake service; back on the road next week.");
        resource("GPS-12", ResourceCategory.GPS_DEVICE, "Handheld GPS unit", ResourceStatus.AVAILABLE, null);
        ResourceItem gps = resource("GPS-13", ResourceCategory.GPS_DEVICE, "Handheld GPS unit",
                ResourceStatus.ALLOCATED, null);
        resource("CAM-07", ResourceCategory.CAMERA, "Camera trap", ResourceStatus.AVAILABLE, null);
        resource("CAM-08", ResourceCategory.CAMERA, "Camera trap", ResourceStatus.AVAILABLE, null);
        ResourceItem radio = resource("RAD-21", ResourceCategory.RADIO, "VHF radio set", ResourceStatus.ALLOCATED, null);
        resource("RAD-22", ResourceCategory.RADIO, "VHF radio set", ResourceStatus.AVAILABLE, null);
        ResourceItem kit = resource("MK-03", ResourceCategory.MEDICAL_KIT, "Field medical kit",
                ResourceStatus.ALLOCATED, null);
        resource("MK-04", ResourceCategory.MEDICAL_KIT, "Field medical kit", ResourceStatus.AVAILABLE, null);
        resource("DRN-01", ResourceCategory.OTHER, "Survey drone", ResourceStatus.AVAILABLE, "Two spare batteries.");

        // who has what right now (releasedAt is empty = still in use)
        allocation(van, AllocationTarget.RESCUE_CASE, String.valueOf(leopard.getId()), "RES-031", vet,
                now.minusDays(4), null);
        allocation(kit, AllocationTarget.PATROL, "PAT-205", "PAT-205", officer, now.minusDays(1), null);
        allocation(gps, AllocationTarget.PATROL, "PAT-204", "PAT-204", officer, now.minusHours(2), null);
        allocation(radio, AllocationTarget.PATROL, "PAT-204", "PAT-204", officer, now.minusHours(2), null);
        // one old allocation that was already given back, so the history is not empty
        allocation(van, AllocationTarget.RESCUE_CASE, String.valueOf(released.getId()), "RES-029", vet,
                now.minusDays(19), now.minusDays(18));

        // ---------- programmes ----------
        LocalDate today = LocalDate.now();

        // 4 + 5 + 3 = 12 of 20 -> 60 %
        Programme water = programme("PRG-001", "Water Source Restoration",
                "Desilt and repair 20 water sources before the dry season.", 20, "water sources",
                today.minusDays(70), today.plusDays(65), ProgrammeStatus.ACTIVE, "Team Bravo");
        activity(water, "Desilted the waterholes of the Zone B forest core.", 4, now.minusDays(43));
        activity(water, "Repaired solar pumps and troughs in Zone A.", 5, now.minusDays(20));
        activity(water, "Restored the river-fed pools of Zone C.", 3, now.minusDays(1));

        // 10 + 15 + 10 = 35 of 50 -> 70 %
        Programme grass = programme("PRG-002", "Grassland Invasive Removal",
                "Clear lantana from 50 hectares of the eastern grassland.", 50, "hectares",
                today.minusDays(87), today.plusDays(50), ProgrammeStatus.ACTIVE, "Team Alpha");
        activity(grass, "Lantana cleared between trail markers 1 and 3.", 10, now.minusDays(61));
        activity(grass, "Lantana cleared between trail markers 3 and 5.", 15, now.minusDays(35));
        activity(grass, "Follow-up clearing and native grass seeding.", 10, now.minusDays(8));

        // 20 + 20 = 40 of 40 -> 100 %, completed
        Programme cameras = programme("PRG-003", "Camera Trap Survey",
                "Install and service 40 camera stations for the annual count.", 40, "camera stations",
                today.minusDays(130), today.minusDays(10), ProgrammeStatus.COMPLETED, "Team Charlie");
        activity(cameras, "Stations 1 to 20 installed.", 20, now.minusDays(92));
        activity(cameras, "Stations 21 to 40 installed and all cards collected.", 20, now.minusDays(18));

        // 3 of 12 -> 25 % and the due date is over -> shown as overdue
        Programme camps = programme("PRG-004", "Community Awareness Camps",
                "Hold 12 awareness camps in the villages around the reserve.", 12, "camps",
                today.minusDays(90), today.minusDays(5), ProgrammeStatus.ACTIVE, null);
        activity(camps, "Camps held in three villages on the eastern boundary.", 3, now.minusDays(30));

        System.out.println("conservation-service: demo data loaded.");
    }

    // ---------- small helper methods so the code above stays short ----------

    private RescueCase rescue(String code, String incident, String animal, String staff, RescueStage stage,
            String summary, LocalDateTime openedAt) {
        RescueCase rescue = new RescueCase();
        rescue.setRescueCode(code);
        rescue.setIncidentId(incident);
        rescue.setIncidentCode(incident);
        rescue.setAnimalId(animal);
        rescue.setAnimalCode(animal);
        rescue.setAssignedStaffName(staff);
        rescue.setStage(stage);
        rescue.setSummary(summary);
        rescue.setReleaseReviewStatus(ReleaseReviewStatus.NONE);
        rescue.setOpenedByName("Dr. Kavya Iyer");
        rescue.setOpenedAt(openedAt);
        return rescue;
    }

    private void history(RescueCase rescue, RescueStage oldStage, RescueStage newStage, String action, String notes,
            String person, LocalDateTime at) {
        RescueStageHistory history = new RescueStageHistory();
        history.setRescueCase(rescue);
        history.setOldStage(oldStage);
        history.setNewStage(newStage);
        history.setAction(action);
        history.setNotes(notes);
        history.setChangedByName(person);
        history.setChangedAt(at);
        historyRepository.save(history);
    }

    private void treatment(RescueCase rescue, String text, String notes, String person, LocalDateTime at) {
        TreatmentRecord treatment = new TreatmentRecord();
        treatment.setRescueCase(rescue);
        treatment.setTreatment(text);
        treatment.setNotes(notes);
        treatment.setRecordedByName(person);
        treatment.setRecordedAt(at);
        treatmentRepository.save(treatment);
    }

    private ResourceItem resource(String code, ResourceCategory category, String name, ResourceStatus status,
            String notes) {
        ResourceItem resource = new ResourceItem();
        resource.setResourceCode(code);
        resource.setCategory(category);
        resource.setName(name);
        resource.setStatus(status);
        resource.setNotes(notes);
        return resourceRepository.save(resource);
    }

    private void allocation(ResourceItem resource, AllocationTarget type, String targetId, String targetCode,
            String person, LocalDateTime allocatedAt, LocalDateTime releasedAt) {
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setResource(resource);
        allocation.setTargetType(type);
        allocation.setTargetId(targetId);
        allocation.setTargetCode(targetCode);
        allocation.setAllocatedByName(person);
        allocation.setAllocatedAt(allocatedAt);
        allocation.setReleasedAt(releasedAt);
        if (releasedAt != null) {
            allocation.setReleasedByName(person);
        }
        allocation.setOverrideUsed(false);
        allocationRepository.save(allocation);
    }

    private Programme programme(String code, String name, String description, int target, String unit,
            LocalDate start, LocalDate due, ProgrammeStatus status, String team) {
        Programme programme = new Programme();
        programme.setProgrammeCode(code);
        programme.setName(name);
        programme.setDescription(description);
        programme.setTargetQuantity(target);
        programme.setUnit(unit);
        programme.setStartDate(start);
        programme.setDueDate(due);
        programme.setStatus(status);
        programme.setOwnerStaffName("Rohan Desai");
        programme.setTeam(team);
        return programmeRepository.save(programme);
    }

    private void activity(Programme programme, String description, int quantity, LocalDateTime at) {
        ProgrammeActivity activity = new ProgrammeActivity();
        activity.setProgramme(programme);
        activity.setDescription(description);
        activity.setQuantityCompleted(quantity);
        activity.setCompletedAt(at);
        activity.setRecordedByName("Rohan Desai");
        activityRepository.save(activity);
    }
}
