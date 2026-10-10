package com.wildguard.conservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.dto.ActivityRequest;
import com.wildguard.conservation.dto.ProgrammeDto;
import com.wildguard.conservation.dto.ProgrammeRequest;
import com.wildguard.conservation.entity.ProgrammeStatus;
import com.wildguard.conservation.service.ProgrammeService;

import jakarta.validation.Valid;

// REST API for conservation programmes. Everyone can read, only admin and manager can change.
@RestController
@RequestMapping("/api/programmes")
public class ProgrammeController {

    private static final String WRITE_ROLES = "hasAnyRole('ADMIN','FOREST_OFFICER')";

    private ProgrammeService programmeService;

    public ProgrammeController(ProgrammeService programmeService) {
        this.programmeService = programmeService;
    }

    @GetMapping
    public List<ProgrammeDto> list(@RequestParam(required = false) ProgrammeStatus status) {
        return programmeService.list(status);
    }

    @GetMapping("/{id}")
    public ProgrammeDto get(@PathVariable Long id) {
        return programmeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(WRITE_ROLES)
    public ProgrammeDto create(@Valid @RequestBody ProgrammeRequest request) {
        return programmeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(WRITE_ROLES)
    public ProgrammeDto update(@PathVariable Long id, @Valid @RequestBody ProgrammeRequest request) {
        return programmeService.update(id, request);
    }

    // POST /api/programmes/1/activities -> record completed work (this changes the progress)
    @PostMapping("/{id}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(WRITE_ROLES)
    public ProgrammeDto addActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return programmeService.addActivity(id, request);
    }
}
