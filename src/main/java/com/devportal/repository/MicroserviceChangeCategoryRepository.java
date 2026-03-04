package com.devportal.repository;

import com.devportal.domain.entity.MicroserviceChangeCategory;
import com.devportal.domain.enums.ChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MicroserviceChangeCategoryRepository extends JpaRepository<MicroserviceChangeCategory, UUID> {
    
    List<MicroserviceChangeCategory> findByFeatureId(UUID featureId);
    
    List<MicroserviceChangeCategory> findByIncidentId(UUID incidentId);
    
    List<MicroserviceChangeCategory> findByHotfixId(UUID hotfixId);
    
    List<MicroserviceChangeCategory> findByIssueId(UUID issueId);
    
    List<MicroserviceChangeCategory> findByMicroserviceId(UUID microserviceId);
    
    List<MicroserviceChangeCategory> findByMicroserviceIdAndChangeType(UUID microserviceId, ChangeType changeType);
    
    void deleteByFeatureId(UUID featureId);
    
    void deleteByIncidentId(UUID incidentId);
    
    void deleteByHotfixId(UUID hotfixId);
    
    void deleteByIssueId(UUID issueId);
}
