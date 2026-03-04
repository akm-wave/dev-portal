package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ChangeType;
import com.devportal.dto.request.ImpactCalculationRequest;
import com.devportal.dto.response.ImpactAnalysisResponse;
import com.devportal.dto.response.ImpactAnalysisResponse.*;
import com.devportal.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ImpactCalculatorService {

    private final MicroserviceRepository microserviceRepository;
    private final MicroserviceChangeCategoryRepository changeCategoryRepository;
    private final ImpactAnalysisRepository impactAnalysisRepository;
    private final ChecklistRepository checklistRepository;
    private final FeatureRepository featureRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final IssueRepository issueRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ImpactAnalysisResponse calculateImpactPreview(ImpactCalculationRequest request) {
        // Calculate impact without saving to DB - for preview/analysis only
        return doCalculateImpact(request, false);
    }

    @Transactional
    public ImpactAnalysisResponse calculateImpact(ImpactCalculationRequest request) {
        return doCalculateImpact(request, true);
    }

    private ImpactAnalysisResponse doCalculateImpact(ImpactCalculationRequest request, boolean persist) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Only save change categories if persisting
        if (persist && request.getFeatureId() != null) {
            saveChangeCategories(request, username);
        }
        
        // Calculate impact
        List<ImpactedMicroservice> impactedMicroservices = new ArrayList<>();
        List<ImpactedArea> impactedAreas = new ArrayList<>();
        List<CriticalChecklist> criticalChecklists = new ArrayList<>();
        List<RecommendedTest> recommendedTests = new ArrayList<>();
        int totalRiskScore = 0;

        Set<String> processedDomains = new HashSet<>();
        Set<UUID> processedChecklistIds = new HashSet<>();

        for (ImpactCalculationRequest.MicroserviceChangeRequest change : request.getMicroserviceChanges()) {
            Microservice ms = microserviceRepository.findById(change.getMicroserviceId())
                    .orElse(null);
            if (ms == null) continue;

            // Calculate risk for this microservice
            int msRiskScore = 0;
            List<String> changeTypeNames = new ArrayList<>();
            
            for (String changeTypeStr : change.getChangeTypes()) {
                try {
                    ChangeType changeType = ChangeType.valueOf(changeTypeStr);
                    msRiskScore += changeType.getRiskWeight();
                    changeTypeNames.add(changeType.getDisplayName());
                    
                    // Add recommended tests based on change type
                    recommendedTests.addAll(getRecommendedTests(changeType, ms.getName()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid change type: {}", changeTypeStr);
                }
            }

            totalRiskScore += msRiskScore;

            impactedMicroservices.add(ImpactedMicroservice.builder()
                    .id(ms.getId())
                    .name(ms.getName())
                    .changeTypes(changeTypeNames)
                    .riskScore(msRiskScore)
                    .riskColor(getRiskColor(msRiskScore))
                    .build());

            // Get impacted areas/domains from features
            if (ms.getFeatures() != null) {
                for (Feature feature : ms.getFeatures()) {
                    String domain = feature.getDomain();
                    if (domain != null && !processedDomains.contains(domain)) {
                        processedDomains.add(domain);
                        impactedAreas.add(ImpactedArea.builder()
                                .name(domain)
                                .domain(domain)
                                .impactLevel(msRiskScore > 8 ? 3 : msRiskScore > 4 ? 2 : 1)
                                .build());
                    }
                }
            }

            // Get critical checklists for this microservice
            List<Checklist> checklists = checklistRepository.findByMicroserviceId(ms.getId());
            for (Checklist checklist : checklists) {
                if (!processedChecklistIds.contains(checklist.getId())) {
                    processedChecklistIds.add(checklist.getId());
                    criticalChecklists.add(CriticalChecklist.builder()
                            .id(checklist.getId())
                            .name(checklist.getName())
                            .priority(checklist.getPriority().name())
                            .status(checklist.getStatus().name())
                            .build());
                }
            }
        }

        String riskLevel = getRiskLevel(totalRiskScore);
        String summary = generateSummary(impactedMicroservices.size(), impactedAreas.size(), 
                criticalChecklists.size(), totalRiskScore, riskLevel);

        ImpactAnalysisResponse.ImpactAnalysisResponseBuilder responseBuilder = ImpactAnalysisResponse.builder()
                .featureId(request.getFeatureId())
                .incidentId(request.getIncidentId())
                .hotfixId(request.getHotfixId())
                .issueId(request.getIssueId())
                .riskScore(totalRiskScore)
                .riskLevel(riskLevel)
                .impactedAreas(impactedAreas)
                .impactedMicroservices(impactedMicroservices)
                .criticalChecklists(criticalChecklists)
                .recommendedTests(recommendedTests)
                .analysisSummary(summary)
                .createdBy(username)
                .createdAt(java.time.LocalDateTime.now());

        // Only save impact analysis if persist flag is true
        if (persist && request.getFeatureId() != null) {
            ImpactAnalysis analysis = saveImpactAnalysis(request, totalRiskScore, riskLevel,
                    impactedAreas, impactedMicroservices, criticalChecklists, recommendedTests, summary, username);
            responseBuilder.id(analysis.getId()).createdAt(analysis.getCreatedAt());
        }

        return responseBuilder
                .build();
    }

    @Transactional(readOnly = true)
    public ImpactAnalysisResponse getLatestAnalysis(UUID featureId, UUID incidentId, UUID hotfixId, UUID issueId) {
        Optional<ImpactAnalysis> analysisOpt = Optional.empty();
        
        if (featureId != null) {
            analysisOpt = impactAnalysisRepository.findFirstByFeatureIdOrderByCreatedAtDesc(featureId);
        } else if (incidentId != null) {
            analysisOpt = impactAnalysisRepository.findFirstByIncidentIdOrderByCreatedAtDesc(incidentId);
        } else if (hotfixId != null) {
            analysisOpt = impactAnalysisRepository.findFirstByHotfixIdOrderByCreatedAtDesc(hotfixId);
        } else if (issueId != null) {
            analysisOpt = impactAnalysisRepository.findFirstByIssueIdOrderByCreatedAtDesc(issueId);
        }

        if (analysisOpt.isEmpty()) {
            return null;
        }

        ImpactAnalysis analysis = analysisOpt.get();
        return mapToResponse(analysis);
    }

    @Transactional(readOnly = true)
    public List<ImpactAnalysisResponse> getAnalysisHistory(UUID featureId) {
        List<ImpactAnalysis> analyses = impactAnalysisRepository.findByFeatureIdOrderByCreatedAtDesc(featureId);
        return analyses.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void saveChangeCategories(ImpactCalculationRequest request, String username) {
        // Delete existing change categories
        if (request.getFeatureId() != null) {
            changeCategoryRepository.deleteByFeatureId(request.getFeatureId());
        } else if (request.getIncidentId() != null) {
            changeCategoryRepository.deleteByIncidentId(request.getIncidentId());
        } else if (request.getHotfixId() != null) {
            changeCategoryRepository.deleteByHotfixId(request.getHotfixId());
        } else if (request.getIssueId() != null) {
            changeCategoryRepository.deleteByIssueId(request.getIssueId());
        }

        // Save new change categories
        for (ImpactCalculationRequest.MicroserviceChangeRequest change : request.getMicroserviceChanges()) {
            Microservice ms = microserviceRepository.findById(change.getMicroserviceId()).orElse(null);
            if (ms == null) continue;

            Feature feature = request.getFeatureId() != null ? 
                    featureRepository.findById(request.getFeatureId()).orElse(null) : null;
            Incident incident = request.getIncidentId() != null ? 
                    incidentRepository.findById(request.getIncidentId()).orElse(null) : null;
            Hotfix hotfix = request.getHotfixId() != null ? 
                    hotfixRepository.findById(request.getHotfixId()).orElse(null) : null;
            Issue issue = request.getIssueId() != null ? 
                    issueRepository.findById(request.getIssueId()).orElse(null) : null;

            for (String changeTypeStr : change.getChangeTypes()) {
                try {
                    ChangeType changeType = ChangeType.valueOf(changeTypeStr);
                    MicroserviceChangeCategory category = MicroserviceChangeCategory.builder()
                            .microservice(ms)
                            .feature(feature)
                            .incident(incident)
                            .hotfix(hotfix)
                            .issue(issue)
                            .changeType(changeType)
                            .notes(change.getNotes())
                            .createdBy(username)
                            .build();
                    changeCategoryRepository.save(category);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid change type: {}", changeTypeStr);
                }
            }
        }
    }

    private ImpactAnalysis saveImpactAnalysis(ImpactCalculationRequest request, int riskScore, String riskLevel,
            List<ImpactedArea> areas, List<ImpactedMicroservice> microservices,
            List<CriticalChecklist> checklists, List<RecommendedTest> tests, String summary, String username) {
        
        Feature feature = request.getFeatureId() != null ? 
                featureRepository.findById(request.getFeatureId()).orElse(null) : null;
        Incident incident = request.getIncidentId() != null ? 
                incidentRepository.findById(request.getIncidentId()).orElse(null) : null;
        Hotfix hotfix = request.getHotfixId() != null ? 
                hotfixRepository.findById(request.getHotfixId()).orElse(null) : null;
        Issue issue = request.getIssueId() != null ? 
                issueRepository.findById(request.getIssueId()).orElse(null) : null;

        ImpactAnalysis analysis = ImpactAnalysis.builder()
                .feature(feature)
                .incident(incident)
                .hotfix(hotfix)
                .issue(issue)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .impactedAreas(toJson(areas))
                .impactedMicroservices(toJson(microservices))
                .criticalChecklists(toJson(checklists))
                .recommendedTests(toJson(tests))
                .analysisSummary(summary)
                .createdBy(username)
                .build();

        return impactAnalysisRepository.save(analysis);
    }

    private List<RecommendedTest> getRecommendedTests(ChangeType changeType, String microserviceName) {
        List<RecommendedTest> tests = new ArrayList<>();
        
        switch (changeType) {
            case CODE_CHANGE:
                tests.add(RecommendedTest.builder()
                        .testType("Unit Test")
                        .description("Run unit tests for modified code")
                        .microserviceName(microserviceName)
                        .priority("HIGH")
                        .build());
                tests.add(RecommendedTest.builder()
                        .testType("Integration Test")
                        .description("Verify integration with dependent services")
                        .microserviceName(microserviceName)
                        .priority("MEDIUM")
                        .build());
                break;
            case DB_CHANGE:
                tests.add(RecommendedTest.builder()
                        .testType("Migration Test")
                        .description("Test database migration scripts")
                        .microserviceName(microserviceName)
                        .priority("CRITICAL")
                        .build());
                tests.add(RecommendedTest.builder()
                        .testType("Data Integrity Test")
                        .description("Verify data integrity after migration")
                        .microserviceName(microserviceName)
                        .priority("CRITICAL")
                        .build());
                break;
            case API_CHANGE:
                tests.add(RecommendedTest.builder()
                        .testType("API Contract Test")
                        .description("Verify API contracts are maintained")
                        .microserviceName(microserviceName)
                        .priority("HIGH")
                        .build());
                tests.add(RecommendedTest.builder()
                        .testType("Consumer Test")
                        .description("Test all API consumers")
                        .microserviceName(microserviceName)
                        .priority("HIGH")
                        .build());
                break;
            case CONFIG_CHANGE:
                tests.add(RecommendedTest.builder()
                        .testType("Config Validation")
                        .description("Validate configuration in all environments")
                        .microserviceName(microserviceName)
                        .priority("MEDIUM")
                        .build());
                break;
            case INFRA_CHANGE:
                tests.add(RecommendedTest.builder()
                        .testType("Deployment Test")
                        .description("Test deployment in staging environment")
                        .microserviceName(microserviceName)
                        .priority("CRITICAL")
                        .build());
                tests.add(RecommendedTest.builder()
                        .testType("Rollback Test")
                        .description("Verify rollback procedure works")
                        .microserviceName(microserviceName)
                        .priority("HIGH")
                        .build());
                break;
        }
        
        return tests;
    }

    private String getRiskLevel(int riskScore) {
        if (riskScore >= 15) return "CRITICAL";
        if (riskScore >= 10) return "HIGH";
        if (riskScore >= 5) return "MEDIUM";
        return "LOW";
    }

    private String getRiskColor(int riskScore) {
        if (riskScore >= 10) return "#ff4d4f";
        if (riskScore >= 6) return "#faad14";
        if (riskScore >= 3) return "#1890ff";
        return "#52c41a";
    }

    private String generateSummary(int msCount, int areaCount, int checklistCount, int riskScore, String riskLevel) {
        return String.format(
                "Impact analysis complete. %d microservice(s) affected across %d area(s). " +
                "%d critical checklist(s) identified. Overall risk score: %d (%s). " +
                "Please review recommended tests before deployment.",
                msCount, areaCount, checklistCount, riskScore, riskLevel
        );
    }

    private ImpactAnalysisResponse mapToResponse(ImpactAnalysis analysis) {
        return ImpactAnalysisResponse.builder()
                .id(analysis.getId())
                .featureId(analysis.getFeature() != null ? analysis.getFeature().getId() : null)
                .incidentId(analysis.getIncident() != null ? analysis.getIncident().getId() : null)
                .hotfixId(analysis.getHotfix() != null ? analysis.getHotfix().getId() : null)
                .issueId(analysis.getIssue() != null ? analysis.getIssue().getId() : null)
                .riskScore(analysis.getRiskScore())
                .riskLevel(analysis.getRiskLevel())
                .impactedAreas(fromJson(analysis.getImpactedAreas(), new TypeReference<List<ImpactedArea>>() {}))
                .impactedMicroservices(fromJson(analysis.getImpactedMicroservices(), new TypeReference<List<ImpactedMicroservice>>() {}))
                .criticalChecklists(fromJson(analysis.getCriticalChecklists(), new TypeReference<List<CriticalChecklist>>() {}))
                .recommendedTests(fromJson(analysis.getRecommendedTests(), new TypeReference<List<RecommendedTest>>() {}))
                .analysisSummary(analysis.getAnalysisSummary())
                .createdBy(analysis.getCreatedBy())
                .createdAt(analysis.getCreatedAt())
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return "[]";
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON", e);
            return null;
        }
    }
}
