package com.wildguard.conservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wildguard.conservation.entity.Programme;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {

    List<Programme> findAllByOrderByProgrammeCodeAsc();

    // next number for the code like PRG-005
    @Query(value = "select nextval('conservation.programme_code_seq')", nativeQuery = true)
    long nextProgrammeNumber();
}
