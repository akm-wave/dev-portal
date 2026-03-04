package com.devportal.repository;

import com.devportal.domain.entity.AiSummary;
import com.devportal.domain.enums.SummaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, UUID> {

    List<AiSummary> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Optional<AiSummary> findByEntityTypeAndEntityIdAndSummaryType(String entityType, UUID entityId, SummaryType summaryType);

    List<AiSummary> findByEntityTypeAndEntityIdOrderByGeneratedAtDesc(String entityType, UUID entityId);

    List<AiSummary> findByIsApprovedFalseOrderByGeneratedAtDesc();
}
