package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.response.HotfixDetailsResponse;
import com.devportal.dto.response.HotfixDetailsResponse.*;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.HotfixChecklistProgressRepository;
import com.devportal.repository.HotfixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotfixDetailsService {

    private final HotfixRepository hotfixRepository;
    private final HotfixChecklistProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public HotfixDetailsResponse getHotfixDetails(UUID hotfixId) {
        Hotfix hotfix = hotfixRepository.findById(hotfixId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotfix not found: " + hotfixId));

        Set<Microservice> uniqueMicroservices = hotfix.getMicroservices();
        
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
            if (progressRepository.findByHotfixIdAndChecklistId(hotfixId, checklist.getId()).isEmpty()) {
                HotfixChecklistProgress newProgress = HotfixChecklistProgress.builder()
                        .hotfix(hotfix)
                        .checklist(checklist)
                        .status(ChecklistStatus.PLANNED)
                        .build();
                progressRepository.save(newProgress);
            }
        }

        List<HotfixChecklistProgress> progressList = progressRepository.findByHotfixIdOrderByCreatedAtAsc(hotfixId);
        Map<UUID, HotfixChecklistProgress> progressMap = progressList.stream()
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
                .filter(c -> c.getHotfixStatus() == ChecklistStatus.COMPLETED)
                .count();
        double overallProgress = allChecklists.isEmpty() ? 0.0 : 
                Math.round((completedCount * 100.0 / allChecklists.size()) * 100.0) / 100.0;

        FeatureSummary featureSummary = FeatureSummary.builder()
                .id(hotfix.getMainFeature().getId().toString())
                .name(hotfix.getMainFeature().getName())
                .domain(hotfix.getMainFeature().getDomain())
                .build();

        UserSummary ownerSummary = hotfix.getOwner() != null ? UserSummary.builder()
                .id(hotfix.getOwner().getId().toString())
                .username(hotfix.getOwner().getUsername())
                .fullName(hotfix.getOwner().getUsername())
                .build() : null;

        return HotfixDetailsResponse.builder()
                .id(hotfix.getId().toString())
                .title(hotfix.getTitle())
                .description(hotfix.getDescription())
                .status(hotfix.getStatus())
                .createdBy(hotfix.getCreatedBy())
                .deployedAt(hotfix.getDeployedAt())
                .createdAt(hotfix.getCreatedAt())
                .updatedAt(hotfix.getUpdatedAt())
                .mainFeature(featureSummary)
                .owner(ownerSummary)
                .totalMicroservices(uniqueMicroservices.size())
                .totalUniqueCheckpoints(allChecklists.size())
                .overallProgress(overallProgress)
                .microservices(microserviceAnalyses)
                .checkpoints(checkpointAnalyses)
                .build();
    }

    private MicroserviceAnalysis buildMicroserviceAnalysis(Microservice ms, Map<UUID, HotfixChecklistProgress> progressMap) {
        Set<Checklist> checklists = ms.getChecklists();
        int total = checklists.size();
        
        long completed = checklists.stream()
                .filter(cl -> {
                    HotfixChecklistProgress progress = progressMap.get(cl.getId());
                    ChecklistStatus status = progress != null ? progress.getStatus() : cl.getStatus();
                    return status == ChecklistStatus.COMPLETED;
                })
                .count();

        double progressPercentage = total == 0 ? 0.0 : Math.round((completed * 100.0 / total) * 100.0) / 100.0;

        List<CheckpointSummary> checkpointSummaries = checklists.stream()
                .map(cl -> {
                    HotfixChecklistProgress progress = progressMap.get(cl.getId());
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
            Map<UUID, HotfixChecklistProgress> progressMap) {
        
        HotfixChecklistProgress progress = progressMap.get(cl.getId());
        
        return CheckpointAnalysis.builder()
                .id(cl.getId().toString())
                .name(cl.getName())
                .description(cl.getDescription())
                .originalStatus(cl.getStatus())
                .hotfixStatus(progress != null ? progress.getStatus() : cl.getStatus())
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
