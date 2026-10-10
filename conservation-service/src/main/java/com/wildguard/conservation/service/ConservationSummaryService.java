package com.wildguard.conservation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.dto.AttentionItem;
import com.wildguard.conservation.dto.LabelValue;
import com.wildguard.conservation.dto.ConservationSummary;
import com.wildguard.conservation.dto.ProgrammeDto;
import com.wildguard.conservation.entity.Programme;
import com.wildguard.conservation.entity.ProgrammeStatus;
import com.wildguard.conservation.entity.ReleaseReviewStatus;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.RescueStage;
import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.entity.ResourceStatus;
import com.wildguard.conservation.repository.ProgrammeRepository;
import com.wildguard.conservation.repository.RescueCaseRepository;
import com.wildguard.conservation.repository.ResourceItemRepository;

// Makes the numbers of my service for the dashboard (rescue, resources, programmes).
// Nothing is hardcoded, everything is counted from the tables.
@Service
public class ConservationSummaryService {

    private RescueCaseRepository rescueRepository;
    private ResourceItemRepository resourceRepository;
    private ProgrammeRepository programmeRepository;
    private ProgrammeService programmeService;

    public ConservationSummaryService(RescueCaseRepository rescueRepository,
            ResourceItemRepository resourceRepository, ProgrammeRepository programmeRepository,
            ProgrammeService programmeService) {
        this.rescueRepository = rescueRepository;
        this.resourceRepository = resourceRepository;
        this.programmeRepository = programmeRepository;
        this.programmeService = programmeService;
    }

    @Transactional(readOnly = true)
    public ConservationSummary summary() {
        // "attention" items are the alerts shown in the attention queue of the dashboard
        List<AttentionItem> attention = new ArrayList<>();

        // --- rescue cases ---
        long activeRescues = 0;
        long pendingReviews = 0;
        // one counter for each stage, in the same order as the enum
        RescueStage[] stages = RescueStage.values();
        long[] stageCounts = new long[stages.length];

        for (RescueCase rescue : rescueRepository.findAll()) {
            stageCounts[rescue.getStage().ordinal()]++;
            if (rescue.getStage().isActive()) {
                activeRescues++;
            }
            if (rescue.getReleaseReviewStatus() == ReleaseReviewStatus.REQUESTED) {
                pendingReviews++;
                attention.add(new AttentionItem("PENDING_RELEASE_REVIEW", "HIGH",
                        rescue.getRescueCode() + " - release review pending",
                        "A rescue officer requested a release review. An officer must record the decision.",
                        "RESCUE", rescue.getId()));
            }
        }
        // data for the "rescue case stages" chart
        List<LabelValue> rescuesByStage = new ArrayList<>();
        for (int i = 0; i < stages.length; i++) {
            rescuesByStage.add(new LabelValue(TextUtil.label(stages[i]), stageCounts[i]));
        }

        // --- resources under maintenance are shown as alerts ---
        for (ResourceItem resource : resourceRepository.findByStatusOrderByResourceCodeAsc(ResourceStatus.MAINTENANCE)) {
            String detail = "Under maintenance.";
            if (resource.getNotes() != null) {
                detail = resource.getNotes();
            }
            attention.add(new AttentionItem("RESOURCE_UNAVAILABLE", "MEDIUM",
                    resource.getResourceCode() + " - " + resource.getName() + " unavailable", detail,
                    "RESOURCE", resource.getId()));
        }

        // --- programmes: progress bars on the dashboard ---
        List<LabelValue> programmeProgress = new ArrayList<>();
        for (Programme programme : programmeRepository.findAllByOrderByProgrammeCodeAsc()) {
            if (programme.getStatus() == ProgrammeStatus.PLANNED) {
                continue; // not started yet, so we dont show it
            }
            ProgrammeDto dto = programmeService.toDto(programme, false);
            programmeProgress.add(new LabelValue(programme.getName(), dto.getProgressPercent()));
            if (dto.getOverdue()) {
                attention.add(new AttentionItem("PROGRAMME_OVERDUE", "MEDIUM",
                        programme.getProgrammeCode() + " - " + programme.getName() + " is past its due date",
                        dto.getProgressPercent() + "% complete, due " + programme.getDueDate() + ".",
                        "PROGRAMME", programme.getId()));
            }
        }

        ConservationSummary summary = new ConservationSummary();
        summary.setActiveRescues(activeRescues);
        summary.setPendingReleaseReviews(pendingReviews);
        summary.setAllocatedResources(resourceRepository.countByStatus(ResourceStatus.ALLOCATED));
        summary.setAvailableResources(resourceRepository.countByStatus(ResourceStatus.AVAILABLE));
        summary.setMaintenanceResources(resourceRepository.countByStatus(ResourceStatus.MAINTENANCE));
        summary.setRescuesByStage(rescuesByStage);
        summary.setProgrammeProgress(programmeProgress);
        summary.setAttention(attention);
        return summary;
    }
}
