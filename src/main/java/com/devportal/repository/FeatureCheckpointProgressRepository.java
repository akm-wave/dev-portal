package com.devportal.repository;

import com.devportal.domain.entity.FeatureCheckpointProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureCheckpointProgressRepository extends JpaRepository<FeatureCheckpointProgress, UUID> {

    List<FeatureCheckpointProgress> findByFeatureId(UUID featureId);

    Optional<FeatureCheckpointProgress> findByFeatureIdAndChecklistId(UUID featureId, UUID checklistId);

    @Query("SELECT fcp FROM FeatureCheckpointProgress fcp WHERE fcp.feature.id = :featureId AND fcp.checklist.id IN :checklistIds")
    List<FeatureCheckpointProgress> findByFeatureIdAndChecklistIds(@Param("featureId") UUID featureId, @Param("checklistIds") List<UUID> checklistIds);

    long countByFeatureIdAndStatus(UUID featureId, com.devportal.domain.enums.ChecklistStatus status);

    void deleteByFeatureIdAndChecklistId(UUID featureId, UUID checklistId);
}
