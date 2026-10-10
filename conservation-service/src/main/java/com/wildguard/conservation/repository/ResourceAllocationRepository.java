package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wildguard.conservation.entity.ResourceAllocation.AllocationTarget;
import com.wildguard.conservation.entity.ResourceAllocation;
import com.wildguard.conservation.entity.ResourceItem;

// @EntityGraph just tells JPA to load the resource together with the allocation in one query.
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {

    // the allocation that is still in use (not released). returns null if the resource is free
    @EntityGraph(attributePaths = "resource")
    ResourceAllocation findFirstByResourceAndReleasedAtIsNull(ResourceItem resource);

    // full history of one resource
    @EntityGraph(attributePaths = "resource")
    List<ResourceAllocation> findByResourceOrderByAllocatedAtDesc(ResourceItem resource);

    // everything that is in use right now
    @EntityGraph(attributePaths = "resource")
    List<ResourceAllocation> findByReleasedAtIsNullOrderByAllocatedAtDesc();

    @EntityGraph(attributePaths = "resource")
    List<ResourceAllocation> findAllByOrderByAllocatedAtDesc();

    // allocations of one patrol or one rescue case
    @EntityGraph(attributePaths = "resource")
    List<ResourceAllocation> findByTargetTypeAndTargetIdOrderByAllocatedAtDesc(AllocationTarget targetType, String targetId);
}
