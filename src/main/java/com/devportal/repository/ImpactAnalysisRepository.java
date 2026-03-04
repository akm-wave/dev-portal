package com.devportal.repository;

import com.devportal.domain.entity.ImpactAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImpactAnalysisRepository extends JpaRepository<ImpactAnalysis, UUID> {
    
    List<ImpactAnalysis> findByFeatureIdOrderByCreatedAtDesc(UUID featureId);
    
    Optional<ImpactAnalysis> findFirstByFeatureIdOrderByCreatedAtDesc(UUID featureId);
    
    List<ImpactAnalysis> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
    
    Optional<ImpactAnalysis> findFirstByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
    
    List<ImpactAnalysis> findByHotfixIdOrderByCreatedAtDesc(UUID hotfixId);
    
    Optional<ImpactAnalysis> findFirstByHotfixIdOrderByCreatedAtDesc(UUID hotfixId);
    
    List<ImpactAnalysis> findByIssueIdOrderByCreatedAtDesc(UUID issueId);
    
    Optional<ImpactAnalysis> findFirstByIssueIdOrderByCreatedAtDesc(UUID issueId);
}
