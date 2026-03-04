package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.request.CheckpointProgressRequest;
import com.devportal.dto.response.FeatureDetailsResponse;
import com.devportal.dto.response.FeatureDetailsResponse.*;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.ChecklistRepository;
import com.devportal.repository.FeatureCheckpointProgressRepository;
import com.devportal.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureDetailsService {

    private final FeatureRepository featureRepository;
    private final FeatureCheckpointProgressRepository progressRepository;
    private final ChecklistRepository checklistRepository;

    @Transactional(readOnly = true)
    public FeatureDetailsResponse getFeatureDetails(UUID featureId) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + featureId));

        Set<Microservice> uniqueMicroservices = feature.getMicroservices();
        
        Map<UUID, Set<String>> checklistToMicroservices = new HashMap<>();
        Map<UUID, Set<String>> checklistToMicroserviceIds = new HashMap<>();
        Set<Checklist> allChecklists = new HashSet<>();

        for (Microservice ms : uniqueMicroservices) {
            for (Checklist cl : ms.getChecklists()) {
                allChecklists.add(cl);
                checklistToMicroservices.computeIfAbsent(cl.getId(), k -> new HashSet<>()).add(ms.getName());
                checklistToMicroserviceIds.computeIfAbsent(cl.getId(), k -> new HashSet<>()).add(ms.getId().toString());
            }
        }

        List<FeatureCheckpointProgress> progressList = progressRepository.findByFeatureId(featureId);
        Map<UUID, FeatureCheckpointProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getChecklist().getId(), p -> p));

        List<MicroserviceAnalysis> microserviceAnalyses = uniqueMicroservices.stream()
                .map(ms -> buildMicroserviceAnalysis(ms, progressMap))
                .sorted(Comparator.comparing(MicroserviceAnalysis::getName))
                .collect(Collectors.toList());

        List<CheckpointAnalysis> checkpointAnalyses = allChecklists.stream()
                .map(cl -> buildCheckpointAnalysis(cl, checklistToMicroservices, checklistToMicroserviceIds, progressMap))
                .sorted(Comparator.comparing(CheckpointAnalysis::getName))
                .collect(Collectors.toList());

        long completedCount = checkpointAnalyses.stream()
                .filter(c -> c.getFeatureStatus() == ChecklistStatus.DONE)
                .count();
        double overallProgress = allChecklists.isEmpty() ? 0.0 : 
                Math.round((completedCount * 100.0 / allChecklists.size()) * 100.0) / 100.0;

        return FeatureDetailsResponse.builder()
                .id(feature.getId().toString())
                .name(feature.getName())
                .description(feature.getDescription())
                .domain(feature.getDomain())
                .status(feature.getStatus())
                .releaseVersion(feature.getReleaseVersion())
                .targetDate(feature.getTargetDate())
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .totalMicroservices(uniqueMicroservices.size())
                .totalUniqueCheckpoints(allChecklists.size())
                .overallProgress(overallProgress)
                .microservices(microserviceAnalyses)
                .checkpoints(checkpointAnalyses)
                .build();
    }

    private MicroserviceAnalysis buildMicroserviceAnalysis(Microservice ms, Map<UUID, FeatureCheckpointProgress> progressMap) {
        Set<Checklist> checklists = ms.getChecklists();
        int total = checklists.size();
        
        long completed = checklists.stream()
                .filter(cl -> {
                    FeatureCheckpointProgress progress = progressMap.get(cl.getId());
                    ChecklistStatus status = progress != null ? progress.getStatus() : cl.getStatus();
                    return status == ChecklistStatus.DONE;
                })
                .count();

        double progressPercentage = total == 0 ? 0.0 : Math.round((completed * 100.0 / total) * 100.0) / 100.0;

        List<CheckpointSummary> checkpointSummaries = checklists.stream()
                .map(cl -> {
                    FeatureCheckpointProgress progress = progressMap.get(cl.getId());
                    ChecklistStatus status = progress != null ? progress.getStatus() : cl.getStatus();
                    return CheckpointSummary.builder()
                            .id(cl.getId().toString())
                            .name(cl.getName())
                            .status(status)
                            .priority(cl.getPriority().name())
                            .build();
                })
                .sorted(Comparator.comparing(CheckpointSummary::getName))
                .collect(Collectors.toList());

        int featureCount = ms.getFeatures() != null ? ms.getFeatures().size() : 0;

        return MicroserviceAnalysis.builder()
                .id(ms.getId().toString())
                .name(ms.getName())
                .description(ms.getDescription())
                .status(ms.getStatus().name())
                .owner(ms.getOwner() != null ? ms.getOwner().getUsername() : null)
                .version(ms.getVersion())
                .progressPercentage(progressPercentage)
                .totalCheckpoints(total)
                .completedCheckpoints((int) completed)
                .highRisk(ms.getHighRisk() != null && ms.getHighRisk())
                .featureCount(featureCount)
                .checkpoints(checkpointSummaries)
                .build();
    }

    private CheckpointAnalysis buildCheckpointAnalysis(
            Checklist cl,
            Map<UUID, Set<String>> checklistToMicroservices,
            Map<UUID, Set<String>> checklistToMicroserviceIds,
            Map<UUID, FeatureCheckpointProgress> progressMap) {
        
        FeatureCheckpointProgress progress = progressMap.get(cl.getId());
        
        return CheckpointAnalysis.builder()
                .id(cl.getId().toString())
                .name(cl.getName())
                .description(cl.getDescription())
                .originalStatus(cl.getStatus())
                .featureStatus(progress != null ? progress.getStatus() : cl.getStatus())
                .priority(cl.getPriority().name())
                .remark(progress != null ? progress.getRemark() : null)
                .attachmentUrl(progress != null ? progress.getAttachmentUrl() : null)
                .updatedBy(progress != null ? progress.getUpdatedBy() : null)
                .updatedAt(progress != null ? progress.getUpdatedAt() : cl.getUpdatedAt())
                .connectedMicroservices(new ArrayList<>(checklistToMicroservices.getOrDefault(cl.getId(), Collections.emptySet())))
                .connectedMicroserviceIds(new ArrayList<>(checklistToMicroserviceIds.getOrDefault(cl.getId(), Collections.emptySet())))
                .build();
    }

    @Transactional
    public CheckpointAnalysis updateCheckpointProgress(UUID featureId, UUID checklistId, CheckpointProgressRequest request) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + featureId));

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found: " + checklistId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        FeatureCheckpointProgress progress = progressRepository
                .findByFeatureIdAndChecklistId(featureId, checklistId)
                .orElseGet(() -> FeatureCheckpointProgress.builder()
                        .feature(feature)
                        .checklist(checklist)
                        .status(ChecklistStatus.PENDING)
                        .build());

        if (request.getStatus() != null) {
            progress.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            progress.setRemark(request.getRemark());
        }
        if (request.getAttachmentUrl() != null) {
            progress.setAttachmentUrl(request.getAttachmentUrl());
        }
        progress.setUpdatedBy(username);

        progress = progressRepository.save(progress);

        Set<String> connectedMicroservices = new HashSet<>();
        Set<String> connectedMicroserviceIds = new HashSet<>();
        for (Microservice ms : feature.getMicroservices()) {
            if (ms.getChecklists().stream().anyMatch(c -> c.getId().equals(checklistId))) {
                connectedMicroservices.add(ms.getName());
                connectedMicroserviceIds.add(ms.getId().toString());
            }
        }

        return CheckpointAnalysis.builder()
                .id(checklist.getId().toString())
                .name(checklist.getName())
                .description(checklist.getDescription())
                .originalStatus(checklist.getStatus())
                .featureStatus(progress.getStatus())
                .priority(checklist.getPriority().name())
                .remark(progress.getRemark())
                .attachmentUrl(progress.getAttachmentUrl())
                .updatedBy(progress.getUpdatedBy())
                .updatedAt(progress.getUpdatedAt())
                .connectedMicroservices(new ArrayList<>(connectedMicroservices))
                .connectedMicroserviceIds(new ArrayList<>(connectedMicroserviceIds))
                .build();
    }

    @Transactional(readOnly = true)
    public List<CheckpointAnalysis> getUniqueCheckpoints(UUID featureId) {
        FeatureDetailsResponse details = getFeatureDetails(featureId);
        return details.getCheckpoints();
    }

    @Transactional
    public void linkCheckpoints(UUID featureId, List<UUID> checklistIds) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + featureId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        for (UUID checklistId : checklistIds) {
            if (progressRepository.findByFeatureIdAndChecklistId(featureId, checklistId).isEmpty()) {
                Checklist checklist = checklistRepository.findById(checklistId)
                        .orElseThrow(() -> new ResourceNotFoundException("Checklist not found: " + checklistId));

                FeatureCheckpointProgress progress = FeatureCheckpointProgress.builder()
                        .feature(feature)
                        .checklist(checklist)
                        .status(checklist.getStatus())
                        .updatedBy(username)
                        .build();
                progressRepository.save(progress);
            }
        }
    }
}
