package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.response.IncidentDetailsResponse;
import com.devportal.dto.response.IncidentDetailsResponse.*;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.IncidentChecklistProgressRepository;
import com.devportal.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentDetailsService {

    private final IncidentRepository incidentRepository;
    private final IncidentChecklistProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public IncidentDetailsResponse getIncidentDetails(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        Set<Microservice> uniqueMicroservices = incident.getMicroservices();
        
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

        // Create progress entries for checklists that don't exist yet
        for (Checklist checklist : allChecklists) {
            if (progressRepository.findByIncidentIdAndChecklistId(incidentId, checklist.getId()).isEmpty()) {
                IncidentChecklistProgress newProgress = IncidentChecklistProgress.builder()
                        .incident(incident)
                        .checklist(checklist)
                        .status(ChecklistStatus.PENDING)
                        .build();
                progressRepository.save(newProgress);
            }
        }

        List<IncidentChecklistProgress> progressList = progressRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);
        Map<UUID, IncidentChecklistProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getChecklist().getId(), p -> p, (a, b) -> a));

        List<MicroserviceAnalysis> microserviceAnalyses = uniqueMicroservices.stream()
                .map(ms -> buildMicroserviceAnalysis(ms, progressMap))
                .sorted(Comparator.comparing(MicroserviceAnalysis::getName))
                .collect(Collectors.toList());

        List<CheckpointAnalysis> checkpointAnalyses = allChecklists.stream()
                .map(cl -> buildCheckpointAnalysis(cl, checklistToMicroservices, checklistToMicroserviceIds, progressMap))
                .sorted(Comparator.comparing(CheckpointAnalysis::getName))
                .collect(Collectors.toList());

        long completedCount = checkpointAnalyses.stream()
                .filter(c -> c.getIncidentStatus() == ChecklistStatus.DONE)
                .count();
        double overallProgress = allChecklists.isEmpty() ? 0.0 : 
                Math.round((completedCount * 100.0 / allChecklists.size()) * 100.0) / 100.0;

        FeatureSummary featureSummary = FeatureSummary.builder()
                .id(incident.getMainFeature().getId().toString())
                .name(incident.getMainFeature().getName())
                .domain(incident.getMainFeature().getDomain())
                .build();

        UserSummary ownerSummary = incident.getOwner() != null ? UserSummary.builder()
                .id(incident.getOwner().getId().toString())
                .username(incident.getOwner().getUsername())
                .fullName(incident.getOwner().getUsername())
                .build() : null;

        return IncidentDetailsResponse.builder()
                .id(incident.getId().toString())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .createdBy(incident.getCreatedBy())
                .resolvedAt(incident.getResolvedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .mainFeature(featureSummary)
                .owner(ownerSummary)
                .totalMicroservices(uniqueMicroservices.size())
                .totalUniqueCheckpoints(allChecklists.size())
                .overallProgress(overallProgress)
                .microservices(microserviceAnalyses)
                .checkpoints(checkpointAnalyses)
                .build();
    }

    private MicroserviceAnalysis buildMicroserviceAnalysis(Microservice ms, Map<UUID, IncidentChecklistProgress> progressMap) {
        Set<Checklist> checklists = ms.getChecklists();
        int total = checklists.size();
        
        long completed = checklists.stream()
                .filter(cl -> {
                    IncidentChecklistProgress progress = progressMap.get(cl.getId());
                    ChecklistStatus status = progress != null ? progress.getStatus() : cl.getStatus();
                    return status == ChecklistStatus.DONE;
                })
                .count();

        double progressPercentage = total == 0 ? 0.0 : Math.round((completed * 100.0 / total) * 100.0) / 100.0;

        List<CheckpointSummary> checkpointSummaries = checklists.stream()
                .map(cl -> {
                    IncidentChecklistProgress progress = progressMap.get(cl.getId());
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
                .checkpoints(checkpointSummaries)
                .build();
    }

    private CheckpointAnalysis buildCheckpointAnalysis(
            Checklist cl,
            Map<UUID, Set<String>> checklistToMicroservices,
            Map<UUID, Set<String>> checklistToMicroserviceIds,
            Map<UUID, IncidentChecklistProgress> progressMap) {
        
        IncidentChecklistProgress progress = progressMap.get(cl.getId());
        
        return CheckpointAnalysis.builder()
                .id(cl.getId().toString())
                .name(cl.getName())
                .description(cl.getDescription())
                .originalStatus(cl.getStatus())
                .incidentStatus(progress != null ? progress.getStatus() : cl.getStatus())
                .priority(cl.getPriority().name())
                .remark(progress != null ? progress.getRemark() : null)
                .mongoFileId(progress != null ? progress.getMongoFileId() : null)
                .attachmentFilename(progress != null ? progress.getAttachmentFilename() : null)
                .updatedBy(progress != null ? progress.getUpdatedBy() : null)
                .updatedAt(progress != null ? progress.getUpdatedAt() : cl.getUpdatedAt())
                .connectedMicroservices(new ArrayList<>(checklistToMicroservices.getOrDefault(cl.getId(), Collections.emptySet())))
                .connectedMicroserviceIds(new ArrayList<>(checklistToMicroserviceIds.getOrDefault(cl.getId(), Collections.emptySet())))
                .build();
    }
}
