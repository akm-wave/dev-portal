package com.devportal.repository;

import com.devportal.domain.entity.ReleaseRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReleaseRecommendationRepository extends JpaRepository<ReleaseRecommendation, UUID> {

    List<ReleaseRecommendation> findByReleaseIdAndIsDismissedFalse(UUID releaseId);

    @Query("SELECT r FROM ReleaseRecommendation r WHERE r.release.id = :releaseId " +
           "AND r.isDismissed = false AND r.isAccepted = false ORDER BY r.recommendationScore DESC")
    List<ReleaseRecommendation> findPendingRecommendations(@Param("releaseId") UUID releaseId);

    List<ReleaseRecommendation> findByReleaseIdAndRecommendedEntityType(UUID releaseId, String entityType);

    void deleteByReleaseId(UUID releaseId);
}
