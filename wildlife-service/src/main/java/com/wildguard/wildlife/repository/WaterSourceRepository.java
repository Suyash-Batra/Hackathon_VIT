package com.wildguard.wildlife.repository;

import com.wildguard.wildlife.entity.WaterSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WaterSourceRepository extends JpaRepository<WaterSource, Long> {
    List<WaterSource> findByZoneId(Long zoneId);
    List<WaterSource> findByStatus(String status);
}