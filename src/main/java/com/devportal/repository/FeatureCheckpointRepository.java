package com.devportal.repository;

import com.devportal.domain.entity.FeatureCheckpoint;
import com.devportal.domain.enums.ChecklistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureCheckpointRepository extends JpaRepository<FeatureCheckpoint, UUID> {

    List<FeatureCheckpoint> findByFeatureId(UUID featureId);

    List<FeatureCheckpoint> findByFeatureIdAndStatus(UUID featureId, ChecklistStatus status);

    Optional<FeatureCheckpoint> findByFeatureIdAndChecklistId(UUID featureId, UUID checklistId);

    @Query("SELECT COUNT(fc) FROM FeatureCheckpoint fc WHERE fc.feature.id = :featureId")
    long countByFeatureId(@Param("featureId") UUID featureId);

    @Query("SELECT COUNT(fc) FROM FeatureCheckpoint fc WHERE fc.feature.id = :featureId AND fc.status = :status")
    long countByFeatureIdAndStatus(@Param("featureId") UUID featureId, @Param("status") ChecklistStatus status);

    @Query("SELECT fc.status, COUNT(fc) FROM FeatureCheckpoint fc WHERE fc.feature.id = :featureId GROUP BY fc.status")
    List<Object[]> countByFeatureIdGroupedByStatus(@Param("featureId") UUID featureId);

    @Query("SELECT COUNT(fc) FROM FeatureCheckpoint fc WHERE fc.status = :status")
    long countAllByStatus(@Param("status") ChecklistStatus status);

    @Query("SELECT fc.status, COUNT(fc) FROM FeatureCheckpoint fc GROUP BY fc.status")
    List<Object[]> countAllGroupedByStatus();

    boolean existsByChecklistId(UUID checklistId);

    void deleteByFeatureId(UUID featureId);
}
