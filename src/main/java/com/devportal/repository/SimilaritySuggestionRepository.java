package com.devportal.repository;

import com.devportal.domain.entity.SimilaritySuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimilaritySuggestionRepository extends JpaRepository<SimilaritySuggestion, UUID> {

    List<SimilaritySuggestion> findBySourceEntityTypeAndSourceEntityIdAndIsDismissedFalse(
            String sourceEntityType, UUID sourceEntityId);

    @Query("SELECT s FROM SimilaritySuggestion s WHERE s.sourceEntityType = :entityType " +
           "AND s.sourceEntityId = :entityId AND s.isDismissed = false ORDER BY s.similarityScore DESC")
    List<SimilaritySuggestion> findTopSimilarItems(
            @Param("entityType") String entityType, 
            @Param("entityId") UUID entityId);

    void deleteBySourceEntityTypeAndSourceEntityId(String sourceEntityType, UUID sourceEntityId);
}
