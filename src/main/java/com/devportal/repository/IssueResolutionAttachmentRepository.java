package com.devportal.repository;

import com.devportal.domain.entity.IssueResolutionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueResolutionAttachmentRepository extends JpaRepository<IssueResolutionAttachment, UUID> {

    List<IssueResolutionAttachment> findByIssueResolutionId(UUID resolutionId);

    void deleteByIssueResolutionId(UUID resolutionId);

    List<IssueResolutionAttachment> findByExtractionStatus(String extractionStatus);
}
