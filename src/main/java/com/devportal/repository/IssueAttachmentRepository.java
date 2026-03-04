package com.devportal.repository;

import com.devportal.domain.entity.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, UUID> {
    List<IssueAttachment> findByIssueIdOrderByCreatedAtDesc(UUID issueId);
    void deleteByIssueId(UUID issueId);
    List<IssueAttachment> findByExtractionStatus(String extractionStatus);
}
