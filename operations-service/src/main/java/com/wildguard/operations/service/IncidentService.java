
package com.wildguard.operations.service;

import com.wildguard.operations.entity.Incident;
import com.wildguard.operations.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public Incident createIncident(Incident incident) {
        if (incident.getStatus() == null || incident.getStatus().isBlank()) {
            incident.setStatus("REPORTED");
        }
        return incidentRepository.save(incident);
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Optional<Incident> getIncidentById(Long id) {
        return incidentRepository.findById(id);
    }

    public Optional<Incident> assignIncident(Long id, String assignedOfficer) {
        return incidentRepository.findById(id).map(incident -> {
            incident.setAssignedOfficer(assignedOfficer);
            incident.setStatus("ASSIGNED");
            return incidentRepository.save(incident);
        });
    }

    public Optional<Incident> updateStatus(Long id, String status) {
        return incidentRepository.findById(id).map(incident -> {
            incident.setStatus(status);
            return incidentRepository.save(incident);
        });
    }

    public void deleteIncident(Long id) {
        incidentRepository.deleteById(id);
    }

    public List<Incident> filterIncidents(String severity, String status) {
    if (severity != null && !severity.isBlank()) {
        return incidentRepository.findBySeverityIgnoreCase(severity);
    }

    if (status != null && !status.isBlank()) {
        return incidentRepository.findByStatusIgnoreCase(status);
    }

    return incidentRepository.findAll();
}
}
