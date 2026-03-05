package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.domain.enums.ModuleType;
import com.devportal.dto.request.ChecklistProgressUpdateRequest;
import com.devportal.dto.response.ChecklistProgressResponse;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentChecklistService {

    private final IncidentRepository incidentRepository;
    private final IncidentChecklistProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final GridFsFileService gridFsFileService;

    @Transactional(readOnly = true)
    public List<ChecklistProgressResponse> getChecklistProgress(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        List<IncidentChecklistProgress> progressList = progressRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);

        // Get all checklists from microservices associated with this incident
        Set<Checklist> allChecklists = incident.getMicroservices().stream()
                .flatMap(m -> m.getChecklists().stream())
                .collect(Collectors.toSet());

        // Create progress entries for checklists that don't have one yet
        for (Checklist checklist : allChecklists) {
            boolean exists = progressList.stream()
                    .anyMatch(p -> p.getChecklist().getId().equals(checklist.getId()));
            if (!exists) {
                IncidentChecklistProgress newProgress = IncidentChecklistProgress.builder()
                        .incident(incident)
                        .checklist(checklist)
                        .status(ChecklistStatus.PLANNED)
                        .build();
                progressRepository.save(newProgress);
            }
        }

        // Refresh the list
        progressList = progressRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);

        return progressList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChecklistProgressResponse updateChecklistStatus(UUID incidentId, UUID checklistId, ChecklistProgressUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        IncidentChecklistProgress progress = progressRepository.findByIncidentIdAndChecklistId(incidentId, checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist progress not found"));

        progress.setStatus(request.getStatus());
        progress.setRemark(request.getRemark());
        progress.setUpdatedBy(username);

        progress = progressRepository.save(progress);
        log.info("Incident {} checklist {} status updated to {} by {}", incidentId, checklistId, request.getStatus(), username);

        return toResponse(progress);
    }

    @Transactional
    public ChecklistProgressResponse uploadAttachment(UUID incidentId, UUID checklistId, MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        IncidentChecklistProgress progress = progressRepository.findByIncidentIdAndChecklistId(incidentId, checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist progress not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Delete old attachment if exists
        if (progress.getMongoFileId() != null) {
            gridFsFileService.deleteFile(progress.getMongoFileId());
        }

        // Upload new attachment to MongoDB GridFS
        String mongoFileId = gridFsFileService.uploadFile(file, ModuleType.INCIDENT_CHECKLIST, progress.getId(), user.getId());

        progress.setMongoFileId(mongoFileId);
        progress.setAttachmentFilename(file.getOriginalFilename());
        progress.setUpdatedBy(username);

        progress = progressRepository.save(progress);
        log.info("Attachment uploaded for incident {} checklist {}: {}", incidentId, checklistId, file.getOriginalFilename());

        return toResponse(progress);
    }

    @Transactional
    public void deleteAttachment(UUID incidentId, UUID checklistId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        IncidentChecklistProgress progress = progressRepository.findByIncidentIdAndChecklistId(incidentId, checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist progress not found"));

        if (progress.getMongoFileId() != null) {
            gridFsFileService.deleteFile(progress.getMongoFileId());
            progress.setMongoFileId(null);
            progress.setAttachmentFilename(null);
            progress.setUpdatedBy(username);
            progressRepository.save(progress);
            log.info("Attachment deleted for incident {} checklist {}", incidentId, checklistId);
        }
    }

    private ChecklistProgressResponse toResponse(IncidentChecklistProgress progress) {
        return ChecklistProgressResponse.builder()
                .id(progress.getId())
                .checklistId(progress.getChecklist().getId())
                .checklistName(progress.getChecklist().getName())
                .checklistDescription(progress.getChecklist().getDescription())
                .status(progress.getStatus())
                .remark(progress.getRemark())
                .mongoFileId(progress.getMongoFileId())
                .attachmentFilename(progress.getAttachmentFilename())
                .updatedBy(progress.getUpdatedBy())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
