package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ModuleType;
import com.devportal.dto.request.IssueCommentRequest;
import com.devportal.dto.request.IssueResolutionRequest;
import com.devportal.dto.response.*;
import com.devportal.exception.ForbiddenException;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueResolutionService {

    private final IssueRepository issueRepository;
    private final IssueAttachmentRepository attachmentRepository;
    private final IssueCommentRepository commentRepository;
    private final IssueResolutionRepository resolutionRepository;
    private final IssueResolutionAttachmentRepository resolutionAttachmentRepository;
    private final UserRepository userRepository;
    private final GridFsFileService gridFsFileService;

    private static final String UPLOAD_DIR = "uploads/issues";
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
    private static final List<String> ALLOWED_DOC_TYPES = List.of("application/pdf", 
            "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    @Transactional(readOnly = true)
    public boolean isOwner(UUID issueId, String username) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        
        if (issue.getOwner() == null) return false;
        return issue.getOwner().getUsername().equals(username);
    }

    @Transactional(readOnly = true)
    public List<IssueAttachmentResponse> getAttachments(UUID issueId) {
        return attachmentRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IssueCommentResponse> getComments(UUID issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueAttachmentResponse uploadAttachment(UUID issueId, MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        // Check if user is owner
        if (issue.getOwner() == null || !issue.getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("Only the issue owner can upload attachments");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOC_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPG, PNG, PDF, DOCX");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create upload directory
        Path uploadPath = Paths.get(UPLOAD_DIR, issueId.toString());
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String newFilename = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(newFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create attachment record
        IssueAttachment attachment = IssueAttachment.builder()
                .issue(issue)
                .fileName(originalFilename)
                .fileType(contentType)
                .fileSize(file.getSize())
                .fileUrl("/api/issues/" + issueId + "/attachments/files/" + newFilename)
                .uploadedBy(user)
                .build();

        attachment = attachmentRepository.save(attachment);
        log.info("Attachment uploaded for issue {}: {}", issueId, originalFilename);

        return toAttachmentResponse(attachment);
    }

    @Transactional
    public void deleteAttachment(UUID issueId, UUID attachmentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        if (issue.getOwner() == null || !issue.getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("Only the issue owner can delete attachments");
        }

        IssueAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        attachmentRepository.delete(attachment);
        log.info("Attachment deleted for issue {}: {}", issueId, attachmentId);
    }

    @Transactional
    public IssueCommentResponse addComment(UUID issueId, IssueCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        // Only owner can add resolution comments
        if (Boolean.TRUE.equals(request.getIsResolutionComment())) {
            if (issue.getOwner() == null || !issue.getOwner().getUsername().equals(username)) {
                throw new ForbiddenException("Only the issue owner can add resolution comments");
            }
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        IssueComment comment = IssueComment.builder()
                .issue(issue)
                .user(user)
                .content(request.getContent())
                .isResolutionComment(request.getIsResolutionComment())
                .build();

        comment = commentRepository.save(comment);
        log.info("Comment added to issue {}: {}", issueId, comment.getId());

        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(UUID issueId, UUID commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        IssueComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        // Only comment author or issue owner can delete
        Issue issue = comment.getIssue();
        boolean isCommentAuthor = comment.getUser().getUsername().equals(username);
        boolean isIssueOwner = issue.getOwner() != null && issue.getOwner().getUsername().equals(username);

        if (!isCommentAuthor && !isIssueOwner) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
        log.info("Comment deleted from issue {}: {}", issueId, commentId);
    }

    private IssueAttachmentResponse toAttachmentResponse(IssueAttachment attachment) {
        return IssueAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .fileUrl(attachment.getFileUrl())
                .uploadedBy(attachment.getUploadedBy() != null ? UserSummary.builder()
                        .id(attachment.getUploadedBy().getId())
                        .username(attachment.getUploadedBy().getUsername())
                        .fullName(attachment.getUploadedBy().getUsername())
                        .build() : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    private IssueCommentResponse toCommentResponse(IssueComment comment) {
        return IssueCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(UserSummary.builder()
                        .id(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .fullName(comment.getUser().getUsername())
                        .build())
                .isResolutionComment(comment.getIsResolutionComment())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    // ==================== NEW RESOLUTION TIMELINE METHODS ====================

    @Transactional(readOnly = true)
    public Page<IssueResolutionResponse> getResolutions(UUID issueId, Pageable pageable) {
        issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        
        return resolutionRepository.findByIssueIdOrderByCreatedAtDesc(issueId, pageable)
                .map(this::toResolutionResponse);
    }

    @Transactional(readOnly = true)
    public List<IssueResolutionResponse> getAllResolutions(UUID issueId) {
        issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        
        return resolutionRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
                .map(this::toResolutionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResolutionResponse createResolution(UUID issueId, IssueResolutionRequest request, List<MultipartFile> files) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        // Check authorization - owner, admin, or lead can add resolutions
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        boolean isOwner = issue.getOwner() != null && issue.getOwner().getUsername().equals(username);
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isLead = user.getRole().name().equals("LEAD");
        
        if (!isOwner && !isAdmin && !isLead) {
            throw new ForbiddenException("Only issue owner, admin, or lead can add resolutions");
        }

        // Create resolution entry
        IssueResolution resolution = IssueResolution.builder()
                .issue(issue)
                .comment(request.getComment())
                .isResolutionComment(request.getIsResolutionComment() != null ? request.getIsResolutionComment() : false)
                .createdBy(user)
                .attachments(new ArrayList<>())
                .build();

        resolution = resolutionRepository.save(resolution);

        // Upload attachments to MongoDB GridFS
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String mongoFileId = gridFsFileService.uploadFile(file, ModuleType.ISSUE_RESOLUTION, resolution.getId(), user.getId());
                    
                    IssueResolutionAttachment attachment = IssueResolutionAttachment.builder()
                            .issueResolution(resolution)
                            .mongoFileId(mongoFileId)
                            .fileName(file.getOriginalFilename())
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .uploadedBy(user)
                            .build();
                    
                    resolutionAttachmentRepository.save(attachment);
                    resolution.getAttachments().add(attachment);
                }
            }
        }

        log.info("Resolution created for issue {}: {}", issueId, resolution.getId());
        return toResolutionResponse(resolution);
    }

    @Transactional
    public IssueResolutionAttachmentResponse addResolutionAttachment(UUID issueId, UUID resolutionId, MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        IssueResolution resolution = resolutionRepository.findById(resolutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resolution not found: " + resolutionId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        boolean isOwner = issue.getOwner() != null && issue.getOwner().getUsername().equals(username);
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isLead = user.getRole().name().equals("LEAD");
        
        if (!isOwner && !isAdmin && !isLead) {
            throw new ForbiddenException("Only issue owner, admin, or lead can add attachments");
        }

        String mongoFileId = gridFsFileService.uploadFile(file, ModuleType.ISSUE_RESOLUTION, resolutionId, user.getId());

        IssueResolutionAttachment attachment = IssueResolutionAttachment.builder()
                .issueResolution(resolution)
                .mongoFileId(mongoFileId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(user)
                .build();

        attachment = resolutionAttachmentRepository.save(attachment);
        log.info("Attachment added to resolution {}: {}", resolutionId, attachment.getId());

        return toResolutionAttachmentResponse(attachment);
    }

    @Transactional
    public void deleteResolutionAttachment(UUID issueId, UUID resolutionId, UUID attachmentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        boolean isOwner = issue.getOwner() != null && issue.getOwner().getUsername().equals(username);
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Only issue owner or admin can delete attachments");
        }

        IssueResolutionAttachment attachment = resolutionAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        // Delete from MongoDB GridFS
        gridFsFileService.deleteFile(attachment.getMongoFileId());
        
        resolutionAttachmentRepository.delete(attachment);
        log.info("Resolution attachment deleted: {}", attachmentId);
    }

    private IssueResolutionResponse toResolutionResponse(IssueResolution resolution) {
        List<IssueResolutionAttachmentResponse> attachmentResponses = resolution.getAttachments() != null
                ? resolution.getAttachments().stream()
                    .map(this::toResolutionAttachmentResponse)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return IssueResolutionResponse.builder()
                .id(resolution.getId().toString())
                .issueId(resolution.getIssue().getId().toString())
                .comment(resolution.getComment())
                .isResolutionComment(resolution.getIsResolutionComment())
                .createdBy(resolution.getCreatedBy() != null ? UserSummary.builder()
                        .id(resolution.getCreatedBy().getId())
                        .username(resolution.getCreatedBy().getUsername())
                        .fullName(resolution.getCreatedBy().getUsername())
                        .build() : null)
                .createdAt(resolution.getCreatedAt())
                .updatedAt(resolution.getUpdatedAt())
                .attachments(attachmentResponses)
                .build();
    }

    private IssueResolutionAttachmentResponse toResolutionAttachmentResponse(IssueResolutionAttachment attachment) {
        return IssueResolutionAttachmentResponse.builder()
                .id(attachment.getId().toString())
                .mongoFileId(attachment.getMongoFileId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .uploadedBy(attachment.getUploadedBy() != null ? UserSummary.builder()
                        .id(attachment.getUploadedBy().getId())
                        .username(attachment.getUploadedBy().getUsername())
                        .fullName(attachment.getUploadedBy().getUsername())
                        .build() : null)
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
