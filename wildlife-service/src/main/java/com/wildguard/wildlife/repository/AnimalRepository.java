package com.wildguard.wildlife.repository;

import com.wildguard.wildlife.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByAnimalCode(String animalCode);
    List<Animal> findByZoneId(Long zoneId);
    List<Animal> findBySpeciesId(Long speciesId);
    List<Animal> findByHealthStatus(String healthStatus);
}