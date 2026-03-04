package com.devportal.service;

import com.devportal.domain.entity.IssueAttachment;
import com.devportal.domain.entity.IssueResolutionAttachment;
import com.devportal.domain.entity.UtilityAttachment;
import com.devportal.repository.IssueAttachmentRepository;
import com.devportal.repository.IssueResolutionAttachmentRepository;
import com.devportal.repository.UtilityAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentExtractionService {

    private final UtilityAttachmentRepository utilityAttachmentRepository;
    private final IssueAttachmentRepository issueAttachmentRepository;
    private final IssueResolutionAttachmentRepository issueResolutionAttachmentRepository;
    private final GridFsFileService gridFsFileService;

    private static final int MAX_CONTENT_LENGTH = 100000; // 100KB of text content

    @Async
    public void extractAndIndexContent(UtilityAttachment attachment) {
        try {
            String content = extractContent(attachment.getFileType(), attachment.getMongoFileId());
            if (content != null) {
                attachment.setContentIndex(truncateContent(content));
                attachment.setExtractionStatus("COMPLETED");
                attachment.setIndexedAt(LocalDateTime.now());
            } else {
                attachment.setExtractionStatus("SKIPPED");
            }
            utilityAttachmentRepository.save(attachment);
            log.info("Indexed content for utility attachment: {}", attachment.getFileName());
        } catch (Exception e) {
            log.error("Failed to extract content from utility attachment: {}", attachment.getFileName(), e);
            attachment.setExtractionStatus("FAILED");
            utilityAttachmentRepository.save(attachment);
        }
    }

    @Async
    public void extractAndIndexContent(IssueAttachment attachment) {
        try {
            String content = extractContentFromUrl(attachment.getFileType(), attachment.getFileUrl());
            if (content != null) {
                attachment.setContentIndex(truncateContent(content));
                attachment.setExtractionStatus("COMPLETED");
                attachment.setIndexedAt(LocalDateTime.now());
            } else {
                attachment.setExtractionStatus("SKIPPED");
            }
            issueAttachmentRepository.save(attachment);
            log.info("Indexed content for issue attachment: {}", attachment.getFileName());
        } catch (Exception e) {
            log.error("Failed to extract content from issue attachment: {}", attachment.getFileName(), e);
            attachment.setExtractionStatus("FAILED");
            issueAttachmentRepository.save(attachment);
        }
    }

    @Async
    public void extractAndIndexContent(IssueResolutionAttachment attachment) {
        try {
            String content = extractContent(attachment.getFileType(), attachment.getMongoFileId());
            if (content != null) {
                attachment.setContentIndex(truncateContent(content));
                attachment.setExtractionStatus("COMPLETED");
                attachment.setIndexedAt(LocalDateTime.now());
            } else {
                attachment.setExtractionStatus("SKIPPED");
            }
            issueResolutionAttachmentRepository.save(attachment);
            log.info("Indexed content for issue resolution attachment: {}", attachment.getFileName());
        } catch (Exception e) {
            log.error("Failed to extract content from issue resolution attachment: {}", attachment.getFileName(), e);
            attachment.setExtractionStatus("FAILED");
            issueResolutionAttachmentRepository.save(attachment);
        }
    }

    private String extractContent(String fileType, String mongoFileId) {
        if (mongoFileId == null || fileType == null) return null;
        
        // Only extract text from .txt files for now
        if (isTextFile(fileType)) {
            try {
                InputStream inputStream = gridFsFileService.getFileAsStream(mongoFileId);
                if (inputStream != null) {
                    return readTextContent(inputStream);
                }
            } catch (Exception e) {
                log.error("Error reading file content: {}", e.getMessage());
            }
        }
        return null;
    }

    private String extractContentFromUrl(String fileType, String fileUrl) {
        if (fileUrl == null || fileType == null) return null;
        
        // Only extract text from .txt files for now
        if (isTextFile(fileType)) {
            try {
                URL url = new URL(fileUrl);
                InputStream inputStream = url.openStream();
                if (inputStream != null) {
                    return readTextContent(inputStream);
                }
            } catch (Exception e) {
                log.error("Error reading file content from URL: {}", e.getMessage());
            }
        }
        return null;
    }

    private boolean isTextFile(String fileType) {
        if (fileType == null) return false;
        String lowerType = fileType.toLowerCase();
        return lowerType.contains("text/plain") || 
               lowerType.endsWith(".txt") ||
               lowerType.equals("txt");
    }

    private String readTextContent(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Error reading text content: {}", e.getMessage());
            return null;
        }
    }

    private String truncateContent(String content) {
        if (content == null) return null;
        if (content.length() > MAX_CONTENT_LENGTH) {
            return content.substring(0, MAX_CONTENT_LENGTH);
        }
        return content;
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void processUnindexedAttachments() {
        log.debug("Processing unindexed attachments...");
        
        // Process utility attachments
        List<UtilityAttachment> utilityAttachments = utilityAttachmentRepository.findByExtractionStatus("PENDING");
        for (UtilityAttachment attachment : utilityAttachments) {
            extractAndIndexContent(attachment);
        }
        
        // Process issue attachments
        List<IssueAttachment> issueAttachments = issueAttachmentRepository.findByExtractionStatus("PENDING");
        for (IssueAttachment attachment : issueAttachments) {
            extractAndIndexContent(attachment);
        }
        
        // Process issue resolution attachments
        List<IssueResolutionAttachment> resolutionAttachments = issueResolutionAttachmentRepository.findByExtractionStatus("PENDING");
        for (IssueResolutionAttachment attachment : resolutionAttachments) {
            extractAndIndexContent(attachment);
        }
        
        if (!utilityAttachments.isEmpty() || !issueAttachments.isEmpty() || !resolutionAttachments.isEmpty()) {
            log.info("Processed {} utility, {} issue, {} resolution attachments for indexing",
                    utilityAttachments.size(), issueAttachments.size(), resolutionAttachments.size());
        }
    }
}
