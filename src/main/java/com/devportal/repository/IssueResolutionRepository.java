package com.devportal.repository;

import com.devportal.domain.entity.IssueResolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueResolutionRepository extends JpaRepository<IssueResolution, UUID> {

    @Query("SELECT r FROM IssueResolution r WHERE r.issue.id = :issueId ORDER BY r.createdAt DESC")
    Page<IssueResolution> findByIssueIdOrderByCreatedAtDesc(@Param("issueId") UUID issueId, Pageable pageable);

    @Query("SELECT r FROM IssueResolution r WHERE r.issue.id = :issueId ORDER BY r.createdAt DESC")
    List<IssueResolution> findByIssueIdOrderByCreatedAtDesc(@Param("issueId") UUID issueId);

    @Query("SELECT COUNT(r) FROM IssueResolution r WHERE r.issue.id = :issueId")
    long countByIssueId(@Param("issueId") UUID issueId);

    void deleteByIssueId(UUID issueId);
}
