package com.wildguard.wildlife.repository;

import com.wildguard.wildlife.entity.ProtectedArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProtectedAreaRepository extends JpaRepository<ProtectedArea, Long> {
    Optional<ProtectedArea> findByAreaCode(String areaCode);
}