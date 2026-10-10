package com.wildguard.conservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.client.ServiceGateway;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.ResourceAllocation.AllocationTarget;
import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.ResourceItem.ResourceCategory;
import com.wildguard.conservation.entity.ResourceItem.ResourceStatus;
import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.repository.ResourceAllocationRepository;
import com.wildguard.conservation.repository.ResourceItemRepository;
import com.wildguard.conservation.util.ErrorHandler.AppException;
import com.wildguard.conservation.util.LoginCheck.LoginUser;
import com.wildguard.conservation.util.TextUtil;

// vehicles and equipment, and giving them to a patrol or a rescue case
@Service
public class ResourceService {

    private ResourceItemRepository resourceRepository;
    private ResourceAllocationRepository allocationRepository;
    private RescueService rescueService;
    private ServiceGateway gateway;

    public ResourceService(ResourceItemRepository resourceRepository, ResourceAllocationRepository allocationRepository,
            RescueService rescueService, ServiceGateway gateway) {
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.rescueService = rescueService;
        this.gateway = gateway;
    }

    public List<ResourceItem> list(ResourceCategory category, ResourceStatus status) {
        List<ResourceItem> result = new ArrayList<>();
        for (ResourceItem resource : resourceRepository.findAllByOrderByResourceCodeAsc()) {
            if (category != null && resource.getCategory() != category) {
                continue;
            }
            if (status != null && resource.getStatus() != status) {
                continue;
            }
            result.add(prepare(resource, false));
        }
        return result;
    }

    public ResourceItem get(Long id) {
        return prepare(find(id), true);
    }

    @Transactional
    public ResourceItem create(ResourceItem form) {
        ResourceItem resource = new ResourceItem();
        copyFields(resource, form);
        resource.setStatus(ResourceStatus.AVAILABLE); // a new resource is free
        resource = resourceRepository.save(resource);
        System.out.println("[RESOURCE] " + resource.getResourceCode() + " added");
        return prepare(resource, false);
    }

    @Transactional
    public ResourceItem update(Long id, ResourceItem form) {
        ResourceItem resource = find(id);
        copyFields(resource, form);
        resourceRepository.save(resource);
        return prepare(resource, false);
    }

    // send to maintenance or bring back. ALLOCATED cannot be set here, only by allocating
    @Transactional
    public ResourceItem changeStatus(Long id, ResourceStatus status) {
        ResourceItem resource = find(id);
        if (status == ResourceStatus.ALLOCATED) {
            throw new AppException(400, "Use the allocation form to allocate a resource.");
        }
        if (allocationRepository.findFirstByResourceAndReleasedAtIsNull(resource) != null) {
            throw new AppException(409, resource.getResourceCode() + " is allocated. Release it before changing its status.");
        }
        resource.setStatus(status);
        resourceRepository.save(resource);
        return prepare(resource, false);
    }

    public List<ResourceAllocation> listAllocations(Boolean activeOnly) {
        if (activeOnly != null && activeOnly) {
            return allocationRepository.findByReleasedAtIsNullOrderByAllocatedAtDesc();
        }
        return allocationRepository.findAllByOrderByAllocatedAtDesc();
    }

    // Gives a resource to a patrol or a rescue case.
    //  1. resource under maintenance cannot be given
    //  2. already with someone -> 409. only officer/admin can override, then the old one is released first
    //  3. the patrol / rescue case must exist and still be running
    //  4. save the allocation and mark the resource ALLOCATED
    // (the controller method is synchronized, so two users cannot be inside here at the same time)
    @Transactional
    public ResourceAllocation allocate(AllocationRequest request, LoginUser user) {
        if (request.resourceId == null || request.targetType == null || TextUtil.clean(request.targetId) == null) {
            throw new AppException(400, "Choose the resource and the patrol or rescue case.");
        }
        ResourceItem resource = find(request.resourceId);
        LocalDateTime now = LocalDateTime.now();

        // 1
        if (resource.getStatus() == ResourceStatus.MAINTENANCE) {
            throw new AppException(409, resource.getResourceCode() + " is under maintenance and cannot be allocated.");
        }

        // 2
        boolean overrideUsed = false;
        ResourceAllocation current = allocationRepository.findFirstByResourceAndReleasedAtIsNull(resource);
        if (current != null) {
            boolean canOverride = user.is("ADMIN", "FOREST_OFFICER");
            boolean wantsOverride = request.override != null && request.override;
            if (!canOverride || !wantsOverride) {
                throw new AppException(409, resource.getResourceCode() + " is already allocated to "
                        + current.getTargetCode() + ". Release it first" + (canOverride ? " or tick the override box." : "."));
            }
            current.setReleasedAt(now);
            current.setReleasedByName(user.email + " (override)");
            // saveAndFlush writes it now, because the database allows only one active allocation per resource
            allocationRepository.saveAndFlush(current);
            overrideUsed = true;
        }

        // 3
        String targetId = request.targetId.trim();
        String targetCode;
        if (request.targetType == AllocationTarget.PATROL) {
            targetCode = gateway.checkPatrol(targetId, user.token); // patrol is in operations-service
        } else {
            targetCode = checkRescueCase(targetId); // rescue case is in my own table
        }

        // 4
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setResource(resource);
        allocation.setTargetType(request.targetType);
        allocation.setTargetId(targetId);
        allocation.setTargetCode(targetCode);
        allocation.setAllocatedAt(now);
        allocation.setAllocatedByName(user.email);
        allocation.setNotes(TextUtil.optional(request.notes, "Notes", 500));
        allocation.setOverrideUsed(overrideUsed);
        allocation = allocationRepository.save(allocation);

        resource.setStatus(ResourceStatus.ALLOCATED);
        resourceRepository.save(resource);
        System.out.println("[RESOURCE] " + resource.getResourceCode() + " allocated to " + targetCode + " by " + user.email);
        return allocation;
    }

    // resource comes back and is free again
    @Transactional
    public ResourceAllocation release(Long allocationId, LoginUser user) {
        ResourceAllocation allocation = allocationRepository.findById(allocationId).orElse(null);
        if (allocation == null) {
            throw new AppException(404, "Allocation " + allocationId + " was not found.");
        }
        if (allocation.getReleasedAt() != null) {
            throw new AppException(409, "This allocation was already released.");
        }
        allocation.setReleasedAt(LocalDateTime.now());
        allocation.setReleasedByName(user.email);
        allocationRepository.save(allocation);

        ResourceItem resource = allocation.getResource();
        resource.setStatus(ResourceStatus.AVAILABLE);
        resourceRepository.save(resource);
        System.out.println("[RESOURCE] " + resource.getResourceCode() + " released by " + user.email);
        return allocation;
    }

    // ---------- helper methods ----------

    private ResourceItem find(Long id) {
        ResourceItem resource = resourceRepository.findById(id).orElse(null);
        if (resource == null) {
            throw new AppException(404, "Resource " + id + " was not found.");
        }
        return resource;
    }

    // used by create and update. the code must not be used by another resource
    private void copyFields(ResourceItem resource, ResourceItem form) {
        String code = TextUtil.required(form.getResourceCode(), "Code", 20).toUpperCase();
        if (!code.equalsIgnoreCase(resource.getResourceCode()) && resourceRepository.existsByResourceCodeIgnoreCase(code)) {
            throw new AppException(409, "Resource code " + code + " is already used.");
        }
        if (form.getCategory() == null) {
            throw new AppException(400, "Category is required.");
        }
        resource.setResourceCode(code);
        resource.setCategory(form.getCategory());
        resource.setName(TextUtil.required(form.getName(), "Name", 120));
        resource.setNotes(TextUtil.optional(form.getNotes(), "Notes", 500));
    }

    // returns the code of the rescue case (RES-031). the case must exist and be open
    private String checkRescueCase(String targetId) {
        RescueCase rescue;
        try {
            rescue = rescueService.find(Long.valueOf(targetId));
        } catch (NumberFormatException | AppException ex) {
            throw new AppException(400, "Rescue case " + targetId + " does not exist.");
        }
        if (!rescue.isActive()) {
            throw new AppException(409, rescue.getRescueCode() + " is closed; resources can only be allocated to active cases.");
        }
        return rescue.getRescueCode();
    }

    // adds who has the resource now, and for the detail page the full history
    private ResourceItem prepare(ResourceItem resource, boolean detail) {
        resource.setCurrentAllocation(allocationRepository.findFirstByResourceAndReleasedAtIsNull(resource));
        if (detail) {
            resource.setAllocations(allocationRepository.findByResourceOrderByAllocatedAtDesc(resource));
        }
        return resource;
    }

    // what the page sends when a resource is given to a patrol or a rescue case.
    // override = true : take it from whoever has it now (only officer / admin can do this)
    public static class AllocationRequest {
        public Long resourceId;
        public AllocationTarget targetType;
        public String targetId;
        public String notes;
        public Boolean override;
    }
}
