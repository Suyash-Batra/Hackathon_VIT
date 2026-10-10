package com.wildguard.conservation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.ResourceItem.ResourceCategory;
import com.wildguard.conservation.entity.ResourceItem.ResourceStatus;
import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.service.ResourceService.AllocationRequest;
import com.wildguard.conservation.service.ResourceService;
import com.wildguard.conservation.util.ErrorHandler.AppException;
import com.wildguard.conservation.util.LoginCheck.LoginUser;
import com.wildguard.conservation.util.LoginCheck;

import jakarta.servlet.http.HttpServletRequest;

// REST API for vehicles / equipment (/api/resources) and for giving them out (/api/allocations).
// Adding and editing resources is for officer and admin. Allocating is also for the rescue officer.
//
// allocate, release and changeStatus are "synchronized": only one thread can run them at a time.
// So if two users press Allocate for the same vehicle in the same second, the second request
// waits until the first is saved, and then gets the "already allocated" error.
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${wildguard.allowed-origins}")
public class ResourceController {

    private ResourceService resourceService;
    private LoginCheck login;

    public ResourceController(ResourceService resourceService, LoginCheck login) {
        this.resourceService = resourceService;
        this.login = login;
    }

    @GetMapping("/resources")
    public List<ResourceItem> list(@RequestParam(required = false) ResourceCategory category,
            @RequestParam(required = false) ResourceStatus status, HttpServletRequest request) {
        login.check(request);
        return resourceService.list(category, status);
    }

    @GetMapping("/resources/{id}")
    public ResourceItem get(@PathVariable Long id, HttpServletRequest request) {
        login.check(request);
        return resourceService.get(id);
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceItem create(@RequestBody ResourceItem form, HttpServletRequest request) {
        login.check(request, "ADMIN", "FOREST_OFFICER");
        return resourceService.create(form);
    }

    @PutMapping("/resources/{id}")
    public ResourceItem update(@PathVariable Long id, @RequestBody ResourceItem form, HttpServletRequest request) {
        login.check(request, "ADMIN", "FOREST_OFFICER");
        return resourceService.update(id, form);
    }

    // body: { "status": "MAINTENANCE" } or AVAILABLE
    @PatchMapping("/resources/{id}/status")
    public synchronized ResourceItem changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        login.check(request, "ADMIN", "FOREST_OFFICER");
        ResourceStatus status;
        try {
            status = ResourceStatus.valueOf(body.get("status"));
        } catch (Exception ex) { // status missing or not one of the enum values
            throw new AppException(400, "Choose a valid status.");
        }
        return resourceService.changeStatus(id, status);
    }

    // GET /api/allocations?activeOnly=true -> only what is in use right now
    @GetMapping("/allocations")
    public List<ResourceAllocation> allocations(@RequestParam(required = false) Boolean activeOnly,
            HttpServletRequest request) {
        login.check(request);
        return resourceService.listAllocations(activeOnly);
    }

    // gives 409 if the resource is already with someone else
    @PostMapping("/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public synchronized ResourceAllocation allocate(@RequestBody AllocationRequest body, HttpServletRequest request) {
        LoginUser user = login.check(request);
        return resourceService.allocate(body, user);
    }

    @PatchMapping("/allocations/{id}/release")
    public synchronized ResourceAllocation release(@PathVariable Long id, HttpServletRequest request) {
        LoginUser user = login.check(request);
        return resourceService.release(id, user);
    }
}
