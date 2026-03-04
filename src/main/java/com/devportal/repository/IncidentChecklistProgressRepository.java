package com.devportal.repository;

import com.devportal.domain.entity.IncidentChecklistProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentChecklistProgressRepository extends JpaRepository<IncidentChecklistProgress, UUID> {
    
    List<IncidentChecklistProgress> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
    
    Optional<IncidentChecklistProgress> findByIncidentIdAndChecklistId(UUID incidentId, UUID checklistId);
    
    void deleteByIncidentId(UUID incidentId);
    
    long countByIncidentId(UUID incidentId);
}
