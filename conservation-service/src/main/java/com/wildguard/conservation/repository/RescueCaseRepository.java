package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wildguard.conservation.entity.RescueCase;
import com.wildguard.conservation.entity.ReleaseReviewStatus;

// Repository for rescue cases. Spring Data writes the SQL for us from the method names.
public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {

    // all cases, newest first
    List<RescueCase> findAllByOrderByOpenedAtDesc();

    // all cases of one incident
    List<RescueCase> findByIncidentId(String incidentId);

    List<RescueCase> findByReleaseReviewStatus(ReleaseReviewStatus status);

    // gives the next number for the code like RES-032 (uses a sequence in the database)
    @Query(value = "select nextval('conservation.rescue_code_seq')", nativeQuery = true)
    long nextRescueNumber();
}
