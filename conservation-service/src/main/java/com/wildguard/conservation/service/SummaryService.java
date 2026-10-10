package com.wildguard.conservation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.wildguard.conservation.entity.Programme.ProgrammeStatus;
import com.wildguard.conservation.entity.Programme;
import com.wildguard.conservation.entity.RescueCase.ReleaseReviewStatus;
import com.wildguard.conservation.entity.RescueCase.RescueStage;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.ResourceItem.ResourceStatus;
import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.repository.ProgrammeRepository;
import com.wildguard.conservation.repository.RescueCaseRepository;
import com.wildguard.conservation.repository.ResourceItemRepository;
import com.wildguard.conservation.util.TextUtil;

// numbers of my part for the cards on the pages and for a dashboard.
// everything is counted from the tables, nothing is typed by hand
@Service
public class SummaryService {

    private RescueCaseRepository rescueRepository;
    private ResourceItemRepository resourceRepository;
    private ProgrammeRepository programmeRepository;
    private ProgrammeService programmeService;

    public SummaryService(RescueCaseRepository rescueRepository, ResourceItemRepository resourceRepository,
            ProgrammeRepository programmeRepository, ProgrammeService programmeService) {
        this.rescueRepository = rescueRepository;
        this.resourceRepository = resourceRepository;
        this.programmeRepository = programmeRepository;
        this.programmeService = programmeService;
    }

    public Map<String, Object> summary() {
        List<Map<String, Object>> attention = new ArrayList<>(); // alerts

        // rescue cases
        long activeRescues = 0;
        long pendingReviews = 0;
        RescueStage[] stages = RescueStage.values();
        long[] stageCounts = new long[stages.length]; // one counter per stage
        for (RescueCase rescue : rescueRepository.findAll()) {
            stageCounts[rescue.getStage().ordinal()]++;
            if (rescue.isActive()) {
                activeRescues++;
            }
            if (rescue.getReleaseReviewStatus() == ReleaseReviewStatus.REQUESTED) {
                pendingReviews++;
                attention.add(alert("PENDING_RELEASE_REVIEW", "HIGH", rescue.getRescueCode() + " - release review pending",
                        "A rescue officer requested a release review. An officer must record the decision.",
                        "RESCUE", rescue.getId()));
            }
        }
        List<Map<String, Object>> rescuesByStage = new ArrayList<>();
        for (int i = 0; i < stages.length; i++) {
            rescuesByStage.add(labelValue(TextUtil.label(stages[i]), stageCounts[i]));
        }

        // resources under maintenance are shown as alerts
        for (ResourceItem resource : resourceRepository.findByStatusOrderByResourceCodeAsc(ResourceStatus.MAINTENANCE)) {
            attention.add(alert("RESOURCE_UNAVAILABLE", "MEDIUM",
                    resource.getResourceCode() + " - " + resource.getName() + " unavailable",
                    resource.getNotes() != null ? resource.getNotes() : "Under maintenance.", "RESOURCE", resource.getId()));
        }

        // programmes that have started
        List<Map<String, Object>> programmeProgress = new ArrayList<>();
        for (Programme programme : programmeRepository.findAllByOrderByProgrammeCodeAsc()) {
            if (programme.getStatus() == ProgrammeStatus.PLANNED) {
                continue;
            }
            programmeService.prepare(programme, false);
            programmeProgress.add(labelValue(programme.getName(), programme.getProgressPercent()));
            if (programme.isOverdue()) {
                attention.add(alert("PROGRAMME_OVERDUE", "MEDIUM",
                        programme.getProgrammeCode() + " - " + programme.getName() + " is past its due date",
                        programme.getProgressPercent() + "% complete, due " + programme.getDueDate() + ".",
                        "PROGRAMME", programme.getId()));
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("activeRescues", activeRescues);
        summary.put("pendingReleaseReviews", pendingReviews);
        summary.put("allocatedResources", resourceRepository.countByStatus(ResourceStatus.ALLOCATED));
        summary.put("availableResources", resourceRepository.countByStatus(ResourceStatus.AVAILABLE));
        summary.put("maintenanceResources", resourceRepository.countByStatus(ResourceStatus.MAINTENANCE));
        summary.put("rescuesByStage", rescuesByStage);
        summary.put("programmeProgress", programmeProgress);
        summary.put("attention", attention);
        return summary;
    }

    private Map<String, Object> labelValue(String label, long value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("label", label);
        map.put("value", value);
        return map;
    }

    private Map<String, Object> alert(String category, String level, String title, String detail, String refType, Long refId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("category", category);
        map.put("level", level);
        map.put("title", title);
        map.put("detail", detail);
        map.put("refType", refType);
        map.put("refId", refId);
        return map;
    }
}
