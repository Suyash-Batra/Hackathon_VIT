package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wildguard.conservation.entity.ProgrammeActivity;

public interface ProgrammeActivityRepository extends JpaRepository<ProgrammeActivity, Long> {

    List<ProgrammeActivity> findByProgrammeIdOrderByCompletedAtDescIdDesc(Long programmeId);

    // adds up all the completed quantities of a programme. coalesce gives 0 if there are no activities.
    // This number is used to calculate the progress %.
    @Query("select coalesce(sum(a.quantityCompleted), 0) from ProgrammeActivity a where a.programme.id = :programmeId")
    long sumCompleted(@Param("programmeId") Long programmeId);
}
