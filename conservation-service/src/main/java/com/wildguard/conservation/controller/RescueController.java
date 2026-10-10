package com.wildguard.conservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.dto.RescueDto;
import com.wildguard.conservation.dto.RescueRequest;
import com.wildguard.conservation.dto.ReviewDecisionRequest;
import com.wildguard.conservation.dto.ReviewRequest;
import com.wildguard.conservation.dto.StageChangeRequest;
import com.wildguard.conservation.dto.TreatmentRequest;
import com.wildguard.conservation.entity.RescueStage;
import com.wildguard.conservation.service.RescueService;

import jakarta.validation.Valid;

// REST API for rescue cases. All urls start with /api/rescues.
// The controller only receives the request and calls the service, the logic is in RescueService.
// @PreAuthorize checks the role of the user, so only the right roles can do each action.
@RestController
@RequestMapping("/api/rescues")
public class RescueController {

    private RescueService rescueService;

    public RescueController(RescueService rescueService) {
        this.rescueService = rescueService;
    }

    // GET /api/rescues  -> list (filters are optional)
    @GetMapping
    public List<RescueDto> list(@RequestParam(required = false) RescueStage stage,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String incidentId,
            @RequestParam(required = false) String animalId) {
        return rescueService.list(stage, activeOnly, incidentId, animalId);
    }

    // GET /api/rescues/31  -> one case with timeline
    @GetMapping("/{id}")
    public RescueDto get(@PathVariable Long id) {
        return rescueService.get(id);
    }

    // POST /api/rescues  -> open a new case (returns 201 Created)
    // @Valid checks the request, for example summary must not be empty
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FOREST_OFFICER','RESCUE_OFFICER')")
    public RescueDto create(@Valid @RequestBody RescueRequest request) {
        return rescueService.create(request);
    }

    // PATCH /api/rescues/31/stage  -> move to the next stage
    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAnyRole('ADMIN','FOREST_OFFICER','RESCUE_OFFICER')")
    public RescueDto changeStage(@PathVariable Long id, @Valid @RequestBody StageChangeRequest request) {
        return rescueService.changeStage(id, request);
    }

    // POST /api/rescues/31/treatments  -> add a treatment note (only rescue officer or admin)
    @PostMapping("/{id}/treatments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RESCUE_OFFICER')")
    public RescueDto addTreatment(@PathVariable Long id, @Valid @RequestBody TreatmentRequest request) {
        return rescueService.addTreatment(id, request);
    }

    // rescue officer asks for the release review
    @PostMapping("/{id}/release-review")
    @PreAuthorize("hasAnyRole('ADMIN','RESCUE_OFFICER')")
    public RescueDto requestReleaseReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return rescueService.requestReleaseReview(id, request.getNotes());
    }

    // officer decides. the rescue officer cannot approve his own request because the roles are different here
    @PostMapping("/{id}/release-decision")
    @PreAuthorize("hasAnyRole('ADMIN','FOREST_OFFICER')")
    public RescueDto decideReleaseReview(@PathVariable Long id, @Valid @RequestBody ReviewDecisionRequest request) {
        return rescueService.decideReleaseReview(id, request);
    }
}
