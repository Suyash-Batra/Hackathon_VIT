
package com.wildguard.operations.repository;

import com.wildguard.operations.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findBySeverityIgnoreCase(String severity);

    List<Incident> findByStatusIgnoreCase(String status);
}
