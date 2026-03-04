package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ModuleType;
import com.devportal.domain.enums.UtilityType;
import com.devportal.dto.request.UtilityRequest;
import com.devportal.dto.response.*;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilityService {

    private final UtilityRepository utilityRepository;
    private final UtilityAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final GridFsFileService gridFsFileService;
    private final UtilityVersionRepository utilityVersionRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UtilityResponse> getAll(int page, int size, String sortBy, String sortDir,
                                                   UtilityType type, String search) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Utility> utilityPage;

        if (search != null && !search.isEmpty() && type != null) {
            utilityPage = utilityRepository.searchByTypeAndTitleOrDescription(type, search, pageable);
        } else if (search != null && !search.isEmpty()) {
            utilityPage = utilityRepository.searchByTitleOrDescription(search, pageable);
        } else if (type != null) {
            utilityPage = utilityRepository.findByType(type, pageable);
        } else {
            utilityPage = utilityRepository.findAll(pageable);
        }

        Page<UtilityResponse> responsePage = utilityPage.map(this::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public UtilityResponse getById(UUID id) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found: " + id));
        return toDetailResponse(utility);
    }

    @Transactional
    public UtilityResponse create(UtilityRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Utility utility = Utility.builder()
                .title(request.getTitle())
                .type(request.getType() != null ? request.getType() : UtilityType.OTHERS)
                .description(request.getDescription())
                .version(request.getVersion())
                .createdBy(user)
                .build();

        utility = utilityRepository.save(utility);
        log.info("Utility created: {}", utility.getTitle());
        return toResponse(utility);
    }

    @Transactional
    public UtilityResponse update(UUID id, UtilityRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found: " + id));

        // Create a version snapshot before updating
        int nextVersionNumber = utilityVersionRepository.findMaxVersionNumber(id).orElse(0) + 1;
        UtilityVersion version = UtilityVersion.builder()
                .utility(utility)
                .versionNumber(nextVersionNumber)
                .title(utility.getTitle())
                .description(utility.getDescription())
                .content(utility.getContent())
                .changeSummary("Updated utility")
                .createdBy(user)
                .build();
        utilityVersionRepository.save(version);
        
        // Update current version number
        utility.setCurrentVersion(nextVersionNumber);

        utility.setTitle(request.getTitle());
        if (request.getType() != null) {
            utility.setType(request.getType());
        }
        utility.setDescription(request.getDescription());
        utility.setVersion(request.getVersion());
        utility.setContent(request.getContent());

        utility = utilityRepository.save(utility);
        log.info("Utility updated: {} (version {})", utility.getTitle(), nextVersionNumber);
        return toResponse(utility);
    }

    @Transactional
    public void delete(UUID id) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found: " + id));
        utilityRepository.delete(utility);
        log.info("Utility deleted: {}", utility.getTitle());
    }

    @Transactional(readOnly = true)
    public List<UtilityAttachmentResponse> getAttachments(UUID utilityId) {
        return attachmentRepository.findByUtilityIdOrderByUploadedAtDesc(utilityId).stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UtilityAttachmentResponse uploadAttachment(UUID utilityId, MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Utility utility = utilityRepository.findById(utilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Utility not found: " + utilityId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Upload to MongoDB GridFS
        String mongoFileId = gridFsFileService.uploadFile(file, ModuleType.UTILITY, utilityId, user.getId());

        UtilityAttachment attachment = UtilityAttachment.builder()
                .utility(utility)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .mongoFileId(mongoFileId)
                .uploadedBy(user)
                .build();

        attachment = attachmentRepository.save(attachment);
        log.info("Attachment uploaded for utility {}: {}", utilityId, file.getOriginalFilename());

        return toAttachmentResponse(attachment);
    }

    @Transactional
    public void deleteAttachment(UUID utilityId, UUID attachmentId) {
        UtilityAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        // Delete from MongoDB GridFS if mongoFileId exists
        if (attachment.getMongoFileId() != null) {
            gridFsFileService.deleteFile(attachment.getMongoFileId());
        }

        attachmentRepository.delete(attachment);
        log.info("Attachment deleted from utility {}: {}", utilityId, attachmentId);
    }

    private UtilityResponse toResponse(Utility utility) {
        return UtilityResponse.builder()
                .id(utility.getId())
                .title(utility.getTitle())
                .type(utility.getType())
                .description(utility.getDescription())
                .version(utility.getVersion())
                .createdBy(utility.getCreatedBy() != null ? UserSummary.builder()
                        .id(utility.getCreatedBy().getId())
                        .username(utility.getCreatedBy().getUsername())
                        .fullName(utility.getCreatedBy().getUsername())
                        .build() : null)
                .createdAt(utility.getCreatedAt())
                .updatedAt(utility.getUpdatedAt())
                .attachmentCount(utility.getAttachments() != null ? utility.getAttachments().size() : 0)
                .build();
    }

    private UtilityResponse toDetailResponse(Utility utility) {
        UtilityResponse response = toResponse(utility);
        response.setAttachments(utility.getAttachments().stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private UtilityAttachmentResponse toAttachmentResponse(UtilityAttachment attachment) {
        return UtilityAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .mongoFileId(attachment.getMongoFileId())
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
