package com.wildguard.conservation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.entity.RescueCase.RescueStage;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.TreatmentRecord;
import com.wildguard.conservation.service.RescueService;
import com.wildguard.conservation.util.ErrorHandler.AppException;
import com.wildguard.conservation.util.LoginCheck.LoginUser;
import com.wildguard.conservation.util.LoginCheck;

import jakarta.servlet.http.HttpServletRequest;

// REST API for rescue cases. The controller only checks who is calling and passes the work
// to RescueService. @CrossOrigin lets the pages of the platform app (port 8081) call this service.
@RestController
@RequestMapping("/api/rescues")
@CrossOrigin(origins = "${wildguard.allowed-origins}")
public class RescueController {

    private RescueService rescueService;
    private LoginCheck login;

    public RescueController(RescueService rescueService, LoginCheck login) {
        this.rescueService = rescueService;
        this.login = login;
    }

    // GET /api/rescues?stage=REHABILITATION&activeOnly=true   (filters are optional)
    @GetMapping
    public List<RescueCase> list(@RequestParam(required = false) RescueStage stage,
            @RequestParam(required = false) Boolean activeOnly, @RequestParam(required = false) String incidentId,
            @RequestParam(required = false) String animalId, HttpServletRequest request) {
        LoginUser user = login.check(request);
        return rescueService.list(stage, activeOnly, incidentId, animalId, user);
    }

    // one case with its timeline
    @GetMapping("/{id}")
    public RescueCase get(@PathVariable Long id, HttpServletRequest request) {
        return rescueService.get(id, login.check(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RescueCase create(@RequestBody RescueCase form, HttpServletRequest request) {
        return rescueService.create(form, login.check(request));
    }

    @PatchMapping("/{id}/stage")
    public RescueCase changeStage(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        return rescueService.changeStage(id, body, login.check(request));
    }

    // only rescue officer (or admin) writes treatment notes
    @PostMapping("/{id}/treatments")
    @ResponseStatus(HttpStatus.CREATED)
    public RescueCase addTreatment(@PathVariable Long id, @RequestBody TreatmentRecord form, HttpServletRequest request) {
        return rescueService.addTreatment(id, form, login.check(request, "ADMIN", "RESCUE_OFFICER"));
    }

    // rescue officer asks for the release review ...
    @PostMapping("/{id}/release-review")
    public RescueCase requestReleaseReview(@PathVariable Long id, @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        return rescueService.requestReleaseReview(id, body.get("notes"), login.check(request, "ADMIN", "RESCUE_OFFICER"));
    }

    // ... and the forest officer decides. different roles, so nobody approves his own request
    @PostMapping("/{id}/release-decision")
    public RescueCase decideReleaseReview(@PathVariable Long id, @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        LoginUser user = login.check(request, "ADMIN", "FOREST_OFFICER");
        if (!(body.get("approved") instanceof Boolean)) {
            throw new AppException(400, "Choose approve or reject.");
        }
        String notes = body.get("notes") == null ? null : body.get("notes").toString();
        return rescueService.decideReleaseReview(id, (Boolean) body.get("approved"), notes, user);
    }
}
