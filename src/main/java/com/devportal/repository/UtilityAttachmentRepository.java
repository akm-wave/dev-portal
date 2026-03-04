package com.devportal.repository;

import com.devportal.domain.entity.UtilityAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UtilityAttachmentRepository extends JpaRepository<UtilityAttachment, UUID> {
    List<UtilityAttachment> findByUtilityIdOrderByUploadedAtDesc(UUID utilityId);
    void deleteByUtilityId(UUID utilityId);
    List<UtilityAttachment> findByExtractionStatus(String extractionStatus);
}
