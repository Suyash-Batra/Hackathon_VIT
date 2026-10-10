package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wildguard.conservation.entity.ResourceItem.ResourceStatus;
import com.wildguard.conservation.entity.ResourceItem;

public interface ResourceItemRepository extends JpaRepository<ResourceItem, Long> {

    List<ResourceItem> findAllByOrderByResourceCodeAsc();

    List<ResourceItem> findByStatusOrderByResourceCodeAsc(ResourceStatus status);

    boolean existsByResourceCodeIgnoreCase(String resourceCode);

    long countByStatus(ResourceStatus status);
}
