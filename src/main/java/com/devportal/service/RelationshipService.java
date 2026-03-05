package com.devportal.service;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.entity.Feature;
import com.devportal.domain.entity.Microservice;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.response.RelationshipResponse;
import com.devportal.dto.response.RelationshipResponse.FeatureNode;
import com.devportal.dto.response.RelationshipResponse.MicroserviceNode;
import com.devportal.dto.response.RelationshipResponse.RelationshipEdge;
import com.devportal.repository.FeatureRepository;
import com.devportal.repository.MicroserviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final MicroserviceRepository microserviceRepository;
    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public RelationshipResponse getRelationships() {
        List<Microservice> allMicroservices = microserviceRepository.findAllWithRelationships();
        List<Feature> allFeatures = featureRepository.findAllWithMicroservices();

        Map<String, Set<String>> microserviceToFeatures = new HashMap<>();
        Map<String, Set<String>> featureToMicroservices = new HashMap<>();
        List<RelationshipEdge> relationships = new ArrayList<>();

        // Build relationship maps
        for (Feature feature : allFeatures) {
            String featureId = feature.getId().toString();
            featureToMicroservices.putIfAbsent(featureId, new HashSet<>());
            
            if (feature.getMicroservices() != null) {
                for (Microservice ms : feature.getMicroservices()) {
                    String msId = ms.getId().toString();
                    
                    microserviceToFeatures.computeIfAbsent(msId, k -> new HashSet<>()).add(featureId);
                    featureToMicroservices.get(featureId).add(msId);
                    
                    relationships.add(RelationshipEdge.builder()
                            .microserviceId(msId)
                            .featureId(featureId)
                            .build());
                }
            }
        }

        // Determine shared features (connected to more than one microservice)
        Set<String> sharedFeatureIds = featureToMicroservices.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Build microservice nodes
        List<MicroserviceNode> microserviceNodes = allMicroservices.stream()
                .map(ms -> {
                    Set<Checklist> checklists = ms.getChecklists();
                    int checklistCount = checklists != null ? checklists.size() : 0;
                    long completedCount = checklists != null ? 
                            checklists.stream().filter(c -> c.getStatus() == ChecklistStatus.COMPLETED).count() : 0;
                    double progress = checklistCount > 0 ? 
                            Math.round((completedCount * 100.0 / checklistCount) * 100.0) / 100.0 : 0.0;

                    Set<String> connectedFeatures = microserviceToFeatures.getOrDefault(ms.getId().toString(), Collections.emptySet());

                    return MicroserviceNode.builder()
                            .id(ms.getId().toString())
                            .name(ms.getName())
                            .description(ms.getDescription())
                            .status(ms.getStatus().name())
                            .owner(ms.getOwner() != null ? ms.getOwner().getUsername() : null)
                            .version(ms.getVersion())
                            .featureCount(connectedFeatures.size())
                            .checklistCount(checklistCount)
                            .completedChecklistCount((int) completedCount)
                            .progressPercentage(progress)
                            .highRisk(ms.getHighRisk() != null && ms.getHighRisk())
                            .technicalDebtScore(ms.getTechnicalDebtScore() != null ? ms.getTechnicalDebtScore() : 0)
                            .build();
                })
                .collect(Collectors.toList());

        // Build feature nodes
        List<FeatureNode> featureNodes = allFeatures.stream()
                .map(f -> {
                    String featureId = f.getId().toString();
                    Set<String> connectedMicroservices = featureToMicroservices.getOrDefault(featureId, Collections.emptySet());

                    return FeatureNode.builder()
                            .id(featureId)
                            .name(f.getName())
                            .description(f.getDescription())
                            .domain(f.getDomain())
                            .status(f.getStatus().name())
                            .releaseVersion(f.getReleaseVersion())
                            .targetDate(f.getTargetDate() != null ? f.getTargetDate().toString() : null)
                            .microserviceCount(connectedMicroservices.size())
                            .isShared(sharedFeatureIds.contains(featureId))
                            .build();
                })
                .collect(Collectors.toList());

        return RelationshipResponse.builder()
                .microservices(microserviceNodes)
                .features(featureNodes)
                .relationships(relationships)
                .microserviceToFeatures(microserviceToFeatures)
                .featureToMicroservices(featureToMicroservices)
                .build();
    }
}
