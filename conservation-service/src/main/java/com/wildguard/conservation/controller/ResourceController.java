package com.wildguard.conservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.dto.AllocationDto;
import com.wildguard.conservation.dto.AllocationRequest;
import com.wildguard.conservation.dto.ResourceDto;
import com.wildguard.conservation.dto.ResourceRequest;
import com.wildguard.conservation.dto.ResourceStatusRequest;
import com.wildguard.conservation.entity.ResourceCategory;
import com.wildguard.conservation.entity.ResourceStatus;
import com.wildguard.conservation.service.ResourceService;

import jakarta.validation.Valid;

// REST API for vehicles/equipment (/api/resources) and for giving them out (/api/allocations).
@RestController
@RequestMapping("/api")
public class ResourceController {

    // roles that can add/edit resources
    private static final String INVENTORY_ROLES = "hasAnyRole('ADMIN','FOREST_OFFICER')";
    // roles that can allocate and release
    private static final String ALLOCATION_ROLES = "hasAnyRole('ADMIN','FOREST_OFFICER','RESCUE_OFFICER')";

    private ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/resources")
    public List<ResourceDto> list(@RequestParam(required = false) ResourceCategory category,
            @RequestParam(required = false) ResourceStatus status) {
        return resourceService.list(category, status);
    }

    @GetMapping("/resources/{id}")
    public ResourceDto get(@PathVariable Long id) {
        return resourceService.get(id);
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(INVENTORY_ROLES)
    public ResourceDto create(@Valid @RequestBody ResourceRequest request) {
        return resourceService.create(request);
    }

    @PutMapping("/resources/{id}")
    @PreAuthorize(INVENTORY_ROLES)
    public ResourceDto update(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(id, request);
    }

    // maintenance on / off
    @PatchMapping("/resources/{id}/status")
    @PreAuthorize(INVENTORY_ROLES)
    public ResourceDto changeStatus(@PathVariable Long id, @Valid @RequestBody ResourceStatusRequest request) {
        return resourceService.changeStatus(id, request.getStatus());
    }

    @GetMapping("/allocations")
    public List<AllocationDto> allocations(@RequestParam(required = false) Boolean activeOnly) {
        return resourceService.listAllocations(activeOnly);
    }

    // POST /api/allocations -> give a resource to a patrol or rescue case.
    // returns 201 if ok, and 409 Conflict if the resource is already given to someone
    @PostMapping("/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ALLOCATION_ROLES)
    public AllocationDto allocate(@Valid @RequestBody AllocationRequest request) {
        return resourceService.allocate(request);
    }

    // PATCH /api/allocations/5/release -> give the resource back
    @PatchMapping("/allocations/{id}/release")
    @PreAuthorize(ALLOCATION_ROLES)
    public AllocationDto release(@PathVariable Long id) {
        return resourceService.release(id);
    }
}
