package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wildguard.conservation.entity.ResourceItem;
import com.wildguard.conservation.entity.ResourceStatus;

import jakarta.persistence.LockModeType;

public interface ResourceItemRepository extends JpaRepository<ResourceItem, Long> {

    List<ResourceItem> findAllByOrderByResourceCodeAsc();

    List<ResourceItem> findByStatusOrderByResourceCodeAsc(ResourceStatus status);

    boolean existsByResourceCodeIgnoreCase(String resourceCode);

    long countByStatus(ResourceStatus status);

    // This one LOCKS the row while we work on it (select ... for update).
    // So if two users try to allocate the same vehicle at the same second,
    // the second one has to wait until the first one is finished.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ResourceItem r where r.id = :id")
    ResourceItem findByIdForUpdate(@Param("id") Long id);
}
