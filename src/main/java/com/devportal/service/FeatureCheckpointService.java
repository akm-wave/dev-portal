package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.domain.enums.ModuleType;
import com.devportal.dto.response.FeatureCheckpointResponse;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureCheckpointService {

    private final FeatureCheckpointRepository featureCheckpointRepository;
    private final FeatureRepository featureRepository;
    private final ChecklistRepository checklistRepository;
    private final UserRepository userRepository;
    private final GridFsFileService gridFsFileService;

    @Transactional(readOnly = true)
    public List<FeatureCheckpointResponse> getCheckpointsByFeatureId(UUID featureId) {
        List<FeatureCheckpoint> checkpoints = featureCheckpointRepository.findByFeatureId(featureId);
        return checkpoints.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeatureCheckpointResponse updateCheckpoint(UUID featureId, UUID checkpointId, ChecklistStatus status, String remark, String attachmentUrl) {
        FeatureCheckpoint checkpoint = featureCheckpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found: " + checkpointId));

        if (!checkpoint.getFeature().getId().equals(featureId)) {
            throw new IllegalArgumentException("Checkpoint does not belong to the specified feature");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (status != null) {
            checkpoint.setStatus(status);
        }
        if (remark != null) {
            checkpoint.setRemark(remark);
        }
        if (attachmentUrl != null) {
            checkpoint.setAttachmentUrl(attachmentUrl);
        }
        checkpoint.setUpdatedBy(username);

        checkpoint = featureCheckpointRepository.save(checkpoint);
        return toResponse(checkpoint);
    }

    @Transactional
    public void generateCheckpointsForFeature(Feature feature, Set<Microservice> microservices) {
        // Collect all checklists from all microservices (deduplicated)
        Set<Checklist> allChecklists = new HashSet<>();
        for (Microservice ms : microservices) {
            if (ms.getChecklists() != null) {
                allChecklists.addAll(ms.getChecklists());
            }
        }

        // Get existing checkpoints for this feature
        Set<UUID> existingChecklistIds = featureCheckpointRepository.findByFeatureId(feature.getId())
                .stream()
                .map(fc -> fc.getChecklist().getId())
                .collect(Collectors.toSet());

        // Create new checkpoints only for checklists not already linked
        List<FeatureCheckpoint> newCheckpoints = new ArrayList<>();
        for (Checklist checklist : allChecklists) {
            if (!existingChecklistIds.contains(checklist.getId())) {
                FeatureCheckpoint checkpoint = FeatureCheckpoint.builder()
                        .feature(feature)
                        .checklist(checklist)
                        .status(ChecklistStatus.PENDING)
                        .build();
                newCheckpoints.add(checkpoint);
            }
        }

        if (!newCheckpoints.isEmpty()) {
            featureCheckpointRepository.saveAll(newCheckpoints);
            log.info("Generated {} checkpoints for feature {}", newCheckpoints.size(), feature.getId());
        }
    }

    @Transactional
    public void syncCheckpointsForFeature(UUID featureId, Set<UUID> microserviceIds) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + featureId));

        Set<Microservice> microservices = feature.getMicroservices();
        generateCheckpointsForFeature(feature, microservices);
    }

    public Map<String, Long> getCheckpointStatsByFeatureId(UUID featureId) {
        List<Object[]> results = featureCheckpointRepository.countByFeatureIdGroupedByStatus(featureId);
        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : results) {
            ChecklistStatus status = (ChecklistStatus) row[0];
            Long count = (Long) row[1];
            stats.put(status.name(), count);
        }
        return stats;
    }

    public double calculateFeatureProgress(UUID featureId) {
        long total = featureCheckpointRepository.countByFeatureId(featureId);
        if (total == 0) return 0.0;
        long done = featureCheckpointRepository.countByFeatureIdAndStatus(featureId, ChecklistStatus.DONE);
        return Math.round((done * 100.0 / total) * 100.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public FeatureCheckpointResponse getCheckpoint(UUID featureId, UUID checkpointId) {
        FeatureCheckpoint checkpoint = featureCheckpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found: " + checkpointId));
        
        if (!checkpoint.getFeature().getId().equals(featureId)) {
            throw new IllegalArgumentException("Checkpoint does not belong to the specified feature");
        }
        
        return toResponse(checkpoint);
    }

    @Transactional
    public FeatureCheckpointResponse uploadAttachment(UUID featureId, UUID checkpointId, MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        FeatureCheckpoint checkpoint = featureCheckpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found: " + checkpointId));
        
        if (!checkpoint.getFeature().getId().equals(featureId)) {
            throw new IllegalArgumentException("Checkpoint does not belong to the specified feature");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Delete old attachment if exists
        if (checkpoint.getMongoFileId() != null) {
            gridFsFileService.deleteFile(checkpoint.getMongoFileId());
        }

        // Upload new attachment to MongoDB GridFS
        String mongoFileId = gridFsFileService.uploadFile(file, ModuleType.FEATURE_CHECKLIST, checkpoint.getId(), user.getId());

        checkpoint.setMongoFileId(mongoFileId);
        checkpoint.setAttachmentFilename(file.getOriginalFilename());
        checkpoint.setUpdatedBy(username);

        checkpoint = featureCheckpointRepository.save(checkpoint);
        log.info("Attachment uploaded for feature {} checkpoint {}: {}", featureId, checkpointId, file.getOriginalFilename());

        return toResponse(checkpoint);
    }

    @Transactional
    public void deleteAttachment(UUID featureId, UUID checkpointId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        FeatureCheckpoint checkpoint = featureCheckpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found: " + checkpointId));
        
        if (!checkpoint.getFeature().getId().equals(featureId)) {
            throw new IllegalArgumentException("Checkpoint does not belong to the specified feature");
        }

        if (checkpoint.getMongoFileId() != null) {
            gridFsFileService.deleteFile(checkpoint.getMongoFileId());
            checkpoint.setMongoFileId(null);
            checkpoint.setAttachmentFilename(null);
            checkpoint.setUpdatedBy(username);
            featureCheckpointRepository.save(checkpoint);
            log.info("Attachment deleted for feature {} checkpoint {}", featureId, checkpointId);
        }
    }

    private FeatureCheckpointResponse toResponse(FeatureCheckpoint checkpoint) {
        return FeatureCheckpointResponse.builder()
                .id(checkpoint.getId())
                .featureId(checkpoint.getFeature().getId())
                .checklistId(checkpoint.getChecklist().getId())
                .checklistName(checkpoint.getChecklist().getName())
                .checklistDescription(checkpoint.getChecklist().getDescription())
                .checklistPriority(checkpoint.getChecklist().getPriority().name())
                .status(checkpoint.getStatus().name())
                .remark(checkpoint.getRemark())
                .attachmentUrl(checkpoint.getAttachmentUrl())
                .mongoFileId(checkpoint.getMongoFileId())
                .attachmentFilename(checkpoint.getAttachmentFilename())
                .updatedBy(checkpoint.getUpdatedBy())
                .createdAt(checkpoint.getCreatedAt())
                .updatedAt(checkpoint.getUpdatedAt())
                .build();
    }
}
