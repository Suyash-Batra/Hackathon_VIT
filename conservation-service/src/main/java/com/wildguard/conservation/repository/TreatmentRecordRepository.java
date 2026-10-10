package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wildguard.conservation.entity.TreatmentRecord;

public interface TreatmentRecordRepository extends JpaRepository<TreatmentRecord, Long> {

    // treatment notes of one rescue case, oldest first
    List<TreatmentRecord> findByRescueCaseIdOrderByRecordedAtAscIdAsc(Long rescueCaseId);
}
