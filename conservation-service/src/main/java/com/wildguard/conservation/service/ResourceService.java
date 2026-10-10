package com.wildguard.conservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wildguard.conservation.service.ActivityLog;
import com.wildguard.conservation.error.ApiException;
import com.wildguard.conservation.security.Caller;
import com.wildguard.conservation.security.Role;
import com.wildguard.conservation.client.ServiceGateway;
import com.wildguard.conservation.dto.AllocationDto;
import com.wildguard.conservation.dto.AllocationRequest;
import com.wildguard.conservation.dto.ResourceDto;
import com.wildguard.conservation.dto.ResourceRequest;
import com.wildguard.conservation.entity.AllocationTarget;
import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.ResourceCategory;
import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.entity.ResourceStatus;
import com.wildguard.conservation.repository.ResourceAllocationRepository;
import com.wildguard.conservation.repository.ResourceItemRepository;

// Vehicles and equipment, and giving them to a patrol or rescue case.
// The main feature here is ALLOCATION VALIDATION: one resource cannot be given twice.
@Service
public class ResourceService {

    private ResourceItemRepository resourceRepository;
    private ResourceAllocationRepository allocationRepository;
    private RescueService rescueService;
    private ServiceGateway gateway;
    private ActivityLog audit;

    public ResourceService(ResourceItemRepository resourceRepository,
            ResourceAllocationRepository allocationRepository, RescueService rescueService, ServiceGateway gateway,
            ActivityLog audit) {
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.rescueService = rescueService;
        this.gateway = gateway;
        this.audit = audit;
    }

    // ---------- inventory ----------

    // list of resources, category and status filters are optional
    @Transactional(readOnly = true)
    public List<ResourceDto> list(ResourceCategory category, ResourceStatus status) {
        List<ResourceDto> result = new ArrayList<>();
        for (ResourceItem resource : resourceRepository.findAllByOrderByResourceCodeAsc()) {
            if (category != null && resource.getCategory() != category) {
                continue;
            }
            if (status != null && resource.getStatus() != status) {
                continue;
            }
            result.add(toDto(resource, false));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ResourceDto get(Long id) {
        return toDto(findResource(id), true);
    }

    @Transactional
    public ResourceDto create(ResourceRequest request) {
        String code = request.getResourceCode().trim().toUpperCase();
        if (resourceRepository.existsByResourceCodeIgnoreCase(code)) {
            throw ApiException.conflict("Resource code " + code + " is already used.");
        }
        ResourceItem resource = new ResourceItem();
        resource.setResourceCode(code);
        resource.setCategory(request.getCategory());
        resource.setName(request.getName().trim());
        resource.setNotes(TextUtil.clean(request.getNotes()));
        resource.setStatus(ResourceStatus.AVAILABLE); // a new resource is free
        resource = resourceRepository.save(resource);

        audit.publish("RESOURCE_CREATED", "RESOURCE", resource.getResourceCode(),
                "Resource " + resource.getResourceCode() + " (" + resource.getName() + ") added.");
        return toDto(resource, false);
    }

    @Transactional
    public ResourceDto update(Long id, ResourceRequest request) {
        ResourceItem resource = findResource(id);
        String code = request.getResourceCode().trim().toUpperCase();
        // if the code was changed, the new code must not be used by another resource
        if (!code.equalsIgnoreCase(resource.getResourceCode()) && resourceRepository.existsByResourceCodeIgnoreCase(code)) {
            throw ApiException.conflict("Resource code " + code + " is already used.");
        }
        resource.setResourceCode(code);
        resource.setCategory(request.getCategory());
        resource.setName(request.getName().trim());
        resource.setNotes(TextUtil.clean(request.getNotes()));
        resourceRepository.save(resource);
        return toDto(resource, false);
    }

    // send to maintenance or bring back. ALLOCATED cannot be set here, only by allocating
    @Transactional
    public ResourceDto changeStatus(Long id, ResourceStatus status) {
        ResourceItem resource = lockResource(id);
        if (status == ResourceStatus.ALLOCATED) {
            throw ApiException.badRequest("Use the allocation form to allocate a resource.");
        }
        if (allocationRepository.findFirstByResourceIdAndReleasedAtIsNull(id) != null) {
            throw ApiException.conflict(resource.getResourceCode() + " is allocated. Release it before changing its status.");
        }
        resource.setStatus(status);
        resourceRepository.save(resource);
        audit.publish("RESOURCE_STATUS_CHANGED", "RESOURCE", resource.getResourceCode(),
                resource.getResourceCode() + " is now " + TextUtil.label(status) + ".");
        return toDto(resource, false);
    }

    // ---------- allocation ----------

    @Transactional(readOnly = true)
    public List<AllocationDto> listAllocations(Boolean activeOnly) {
        List<ResourceAllocation> allocations;
        if (activeOnly != null && activeOnly) {
            allocations = allocationRepository.findByReleasedAtIsNullOrderByAllocatedAtDesc();
        } else {
            allocations = allocationRepository.findAllByOrderByAllocatedAtDesc();
        }
        List<AllocationDto> result = new ArrayList<>();
        for (ResourceAllocation allocation : allocations) {
            result.add(toAllocationDto(allocation));
        }
        return result;
    }

    // Gives a resource to a patrol or rescue case. This is the allocation validation.
    // Steps:
    //  1. lock the resource row so two users cannot do this at the same moment
    //  2. a resource under maintenance cannot be given
    //  3. if it is already given to someone -> error 409 (double allocation is stopped here)
    //     only an officer/admin can tick "override", then the old allocation is released first
    //  4. check the patrol / rescue case really exists and is still running
    //  5. save the allocation and mark the resource as ALLOCATED
    @Transactional
    public AllocationDto allocate(AllocationRequest request) {
        Caller caller = Caller.current();
        LocalDateTime now = LocalDateTime.now();

        // step 1
        ResourceItem resource = lockResource(request.getResourceId());

        // step 2
        if (resource.getStatus() == ResourceStatus.MAINTENANCE) {
            throw ApiException.conflict(resource.getResourceCode() + " is under maintenance and cannot be allocated.");
        }

        // step 3
        boolean overrideUsed = false;
        ResourceAllocation current = allocationRepository.findFirstByResourceIdAndReleasedAtIsNull(resource.getId());
        if (current != null) {
            boolean canOverride = caller.hasRole(Role.ADMIN, Role.FOREST_OFFICER);
            boolean wantsOverride = request.getOverride() != null && request.getOverride();
            if (!wantsOverride || !canOverride) {
                String message = resource.getResourceCode() + " is already allocated to " + current.getTargetCode()
                        + ". Release it first";
                if (canOverride) {
                    message = message + " or tick the override box.";
                } else {
                    message = message + ".";
                }
                throw ApiException.conflict(message);
            }
            // override: release the old one first
            current.setReleasedAt(now);
            current.setReleasedByName(caller.displayName() + " (override)");
            // saveAndFlush writes it to the database right now. we need that because the database
            // has a unique index that allows only one active allocation per resource
            allocationRepository.saveAndFlush(current);
            overrideUsed = true;
        }

        // step 4
        String targetCode = checkTarget(request.getTargetType(), request.getTargetId());

        // step 5
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setResource(resource);
        allocation.setTargetType(request.getTargetType());
        allocation.setTargetId(request.getTargetId().trim());
        allocation.setTargetCode(targetCode);
        allocation.setAllocatedAt(now);
        allocation.setAllocatedByName(caller.displayName());
        allocation.setNotes(TextUtil.clean(request.getNotes()));
        allocation.setOverrideUsed(overrideUsed);
        allocation = allocationRepository.save(allocation);

        resource.setStatus(ResourceStatus.ALLOCATED);
        resourceRepository.save(resource);

        String message = resource.getResourceCode() + " allocated to " + targetCode + " by " + caller.displayName();
        if (overrideUsed) {
            message = message + " (override of the previous allocation)";
        }
        audit.publish("RESOURCE_ALLOCATED", "RESOURCE", resource.getResourceCode(), message + ".");
        return toAllocationDto(allocation);
    }

    // Returns the resource. We set the release time and the resource becomes AVAILABLE again.
    @Transactional
    public AllocationDto release(Long allocationId) {
        Caller caller = Caller.current();
        ResourceAllocation allocation = allocationRepository.findById(allocationId).orElse(null);
        if (allocation == null) {
            throw ApiException.notFound("Allocation " + allocationId + " was not found.");
        }
        if (allocation.getReleasedAt() != null) {
            throw ApiException.conflict("This allocation was already released.");
        }
        ResourceItem resource = lockResource(allocation.getResource().getId());
        allocation.setReleasedAt(LocalDateTime.now());
        allocation.setReleasedByName(caller.displayName());
        allocationRepository.save(allocation);

        resource.setStatus(ResourceStatus.AVAILABLE);
        resourceRepository.save(resource);

        audit.publish("RESOURCE_RELEASED", "RESOURCE", resource.getResourceCode(),
                resource.getResourceCode() + " released from " + allocation.getTargetCode() + " by "
                        + caller.displayName() + ".");
        return toAllocationDto(allocation);
    }

    // ---------- helper methods ----------

    // Checks the patrol or rescue case and returns its code (PAT-205 or RES-031).
    // A patrol is in operations-service so we ask it by REST. A rescue case is in our own table.
    private String checkTarget(AllocationTarget targetType, String targetId) {
        if (targetType == AllocationTarget.PATROL) {
            return gateway.checkPatrol(targetId);
        }

        // for a rescue case the id is the number of our own row
        RescueCase rescue;
        try {
            rescue = rescueService.findCase(Long.valueOf(targetId.trim()));
        } catch (NumberFormatException | ApiException ex) {
            throw ApiException.badRequest("Rescue case " + targetId + " does not exist.");
        }
        if (!rescue.getStage().isActive()) {
            throw ApiException.conflict(rescue.getRescueCode() + " is closed; resources can only be allocated to active cases.");
        }
        return rescue.getRescueCode();
    }

    private ResourceItem findResource(Long id) {
        ResourceItem resource = resourceRepository.findById(id).orElse(null);
        if (resource == null) {
            throw ApiException.notFound("Resource " + id + " was not found.");
        }
        return resource;
    }

    // same as findResource but it also locks the row
    private ResourceItem lockResource(Long id) {
        ResourceItem resource = resourceRepository.findByIdForUpdate(id);
        if (resource == null) {
            throw ApiException.notFound("Resource " + id + " was not found.");
        }
        return resource;
    }

    // entity -> DTO for a resource. detail = true also adds the allocation history
    private ResourceDto toDto(ResourceItem resource, boolean detail) {
        ResourceDto dto = new ResourceDto();
        dto.setId(resource.getId());
        dto.setResourceCode(resource.getResourceCode());
        dto.setCategory(resource.getCategory());
        dto.setName(resource.getName());
        dto.setStatus(resource.getStatus());
        dto.setNotes(resource.getNotes());

        ResourceAllocation current = allocationRepository.findFirstByResourceIdAndReleasedAtIsNull(resource.getId());
        if (current != null) {
            dto.setCurrentAllocation(toAllocationDto(current));
        }

        List<AllocationDto> history = new ArrayList<>();
        if (detail) {
            for (ResourceAllocation allocation : allocationRepository.findByResourceIdOrderByAllocatedAtDesc(resource.getId())) {
                history.add(toAllocationDto(allocation));
            }
        }
        dto.setAllocations(history);
        return dto;
    }

    // entity -> DTO for an allocation. static so RescueService can use it too
    public static AllocationDto toAllocationDto(ResourceAllocation allocation) {
        ResourceItem resource = allocation.getResource();
        AllocationDto dto = new AllocationDto();
        dto.setId(allocation.getId());
        dto.setResourceId(resource.getId());
        dto.setResourceCode(resource.getResourceCode());
        dto.setResourceName(resource.getName());
        dto.setCategory(resource.getCategory());
        dto.setTargetType(allocation.getTargetType());
        dto.setTargetId(allocation.getTargetId());
        dto.setTargetCode(allocation.getTargetCode());
        dto.setAllocatedAt(allocation.getAllocatedAt());
        dto.setAllocatedByName(allocation.getAllocatedByName());
        dto.setReleasedAt(allocation.getReleasedAt());
        dto.setReleasedByName(allocation.getReleasedByName());
        dto.setNotes(allocation.getNotes());
        dto.setOverrideUsed(allocation.getOverrideUsed() != null && allocation.getOverrideUsed());
        dto.setActive(allocation.getReleasedAt() == null);
        return dto;
    }
}
