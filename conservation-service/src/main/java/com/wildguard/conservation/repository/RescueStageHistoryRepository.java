package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wildguard.conservation.entity.RescueStageHistory;

public interface RescueStageHistoryRepository extends JpaRepository<RescueStageHistory, Long> {

    // history of one rescue case, oldest first
    List<RescueStageHistory> findByRescueCaseIdOrderByChangedAtAscIdAsc(Long rescueCaseId);
}
