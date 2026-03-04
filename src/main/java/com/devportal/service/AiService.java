package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.SummaryType;
import com.devportal.dto.response.*;
import com.devportal.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiSummaryRepository summaryRepository;
    private final SimilaritySuggestionRepository similarityRepository;
    private final ReleaseRecommendationRepository recommendationRepository;
    private final IssueRepository issueRepository;
    private final FeatureRepository featureRepository;
    private final ReleaseRepository releaseRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final MicroserviceRepository microserviceRepository;
    private final UserRepository userRepository;

    // ==================== SMART SUMMARIES ====================

    public AiSummaryResponse generateSummary(String entityType, UUID entityId, SummaryType summaryType) {
        String summaryText = generateSummaryText(entityType, entityId, summaryType);

        AiSummary summary = AiSummary.builder()
                .entityType(entityType)
                .entityId(entityId)
                .summaryType(summaryType)
                .summaryText(summaryText)
                .generatedBy("AI_ENGINE")
                .build();

        return mapSummaryToResponse(summaryRepository.save(summary));
    }

    public List<AiSummaryResponse> getSummaries(String entityType, UUID entityId) {
        return summaryRepository.findByEntityTypeAndEntityIdOrderByGeneratedAtDesc(entityType, entityId)
                .stream()
                .map(this::mapSummaryToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AiSummaryResponse approveSummary(UUID summaryId) {
        AiSummary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new EntityNotFoundException("Summary not found"));
        
        User currentUser = getCurrentUser();
        summary.setIsApproved(true);
        summary.setApprovedBy(currentUser);
        summary.setApprovedAt(LocalDateTime.now());

        return mapSummaryToResponse(summaryRepository.save(summary));
    }

    private String generateSummaryText(String entityType, UUID entityId, SummaryType summaryType) {
        switch (entityType.toUpperCase()) {
            case "ISSUE":
                return generateIssueSummary(entityId, summaryType);
            case "RELEASE":
                return generateReleaseSummary(entityId);
            case "INCIDENT":
                return generateIncidentSummary(entityId);
            case "HOTFIX":
                return generateHotfixSummary(entityId);
            default:
                return "Summary generation not supported for this entity type.";
        }
    }

    private String generateIssueSummary(UUID issueId, SummaryType summaryType) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));

        StringBuilder summary = new StringBuilder();
        summary.append("## Issue Summary: ").append(issue.getTitle()).append("\n\n");
        summary.append("**Category:** ").append(issue.getCategory()).append("\n");
        summary.append("**Status:** ").append(issue.getStatus()).append("\n");
        summary.append("**Priority:** ").append(issue.getPriority()).append("\n\n");

        if (issue.getDescription() != null) {
            summary.append("### Description\n");
            summary.append(truncateText(issue.getDescription(), 500)).append("\n\n");
        }

        if (issue.getResultComment() != null) {
            summary.append("### Resolution\n");
            summary.append(truncateText(issue.getResultComment(), 500)).append("\n\n");
        }

        summary.append("### Key Points\n");
        summary.append("- Issue created on ").append(issue.getCreatedAt()).append("\n");
        if (issue.getResolvedAt() != null) {
            summary.append("- Resolved on ").append(issue.getResolvedAt()).append("\n");
        }

        return summary.toString();
    }

    private String generateReleaseSummary(UUID releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found"));

        StringBuilder summary = new StringBuilder();
        summary.append("## Release Notes: ").append(release.getName()).append(" (").append(release.getVersion()).append(")\n\n");
        
        // Release Details
        summary.append("### Release Details\n");
        summary.append("| Field | Value |\n");
        summary.append("|-------|-------|\n");
        summary.append("| **Status** | ").append(release.getStatus()).append(" |\n");
        if (release.getReleaseDate() != null) {
            summary.append("| **Release Date** | ").append(release.getReleaseDate()).append(" |\n");
        }
        if (release.getOldBuildNumber() != null) {
            summary.append("| **Old Build Number** | ").append(release.getOldBuildNumber()).append(" |\n");
        }
        if (release.getFeatureBranch() != null) {
            summary.append("| **Feature Branch** | ").append(release.getFeatureBranch()).append(" |\n");
        }
        summary.append("| **Created By** | ").append(release.getCreatedBy() != null ? release.getCreatedBy().getUsername() : "N/A").append(" |\n");
        summary.append("| **Created At** | ").append(release.getCreatedAt()).append(" |\n");
        summary.append("\n");

        if (release.getDescription() != null) {
            summary.append("### Overview\n");
            summary.append(release.getDescription()).append("\n\n");
        }

        if (!release.getReleaseMicroservices().isEmpty()) {
            summary.append("### Included Microservices\n");
            summary.append("| Microservice | Branch | Build Number | Notes |\n");
            summary.append("|--------------|--------|--------------|-------|\n");
            release.getReleaseMicroservices().forEach(rm -> {
                summary.append("| **").append(rm.getMicroservice().getName()).append("** | ");
                summary.append(rm.getBranchName() != null ? rm.getBranchName() : "-").append(" | ");
                summary.append(rm.getBuildNumber() != null ? rm.getBuildNumber() : "-").append(" | ");
                summary.append(rm.getNotes() != null ? rm.getNotes() : "-").append(" |\n");
                
                // Add checklist status for each microservice
                var checklists = rm.getMicroservice().getChecklists();
                if (checklists != null && !checklists.isEmpty()) {
                    long completed = checklists.stream().filter(c -> "COMPLETED".equals(c.getStatus().name())).count();
                    long total = checklists.size();
                    summary.append("| └─ Checklist Status | ").append(completed).append("/").append(total).append(" completed | | |\n");
                }
            });
            summary.append("\n");
        }

        if (!release.getReleaseLinks().isEmpty()) {
            summary.append("### Linked Items\n");
            summary.append("| Type | ID | Name |\n");
            summary.append("|------|----|----- |\n");
            release.getReleaseLinks().forEach(link -> {
                String entityName = getEntityNameForSummary(link.getEntityType(), link.getEntityId());
                summary.append("| ").append(link.getEntityType()).append(" | ");
                summary.append(link.getEntityId().toString().substring(0, 8)).append("... | ");
                summary.append(entityName).append(" |\n");
            });
            summary.append("\n");
        }

        // Detailed linked items by type
        var featureLinks = release.getReleaseLinks().stream()
                .filter(l -> l.getEntityType().name().equals("FEATURE")).toList();
        var issueLinks = release.getReleaseLinks().stream()
                .filter(l -> l.getEntityType().name().equals("ISSUE")).toList();
        var hotfixLinks = release.getReleaseLinks().stream()
                .filter(l -> l.getEntityType().name().equals("HOTFIX")).toList();
        var incidentLinks = release.getReleaseLinks().stream()
                .filter(l -> l.getEntityType().name().equals("INCIDENT")).toList();
        
        if (!featureLinks.isEmpty()) {
            summary.append("### Features (").append(featureLinks.size()).append(")\n");
            featureLinks.forEach(link -> {
                var feature = featureRepository.findById(link.getEntityId()).orElse(null);
                if (feature != null) {
                    summary.append("- **").append(feature.getName()).append("**\n");
                    summary.append("  - Domain: ").append(feature.getDomain()).append("\n");
                    summary.append("  - Status: ").append(feature.getStatus()).append("\n");
                    if (feature.getDescription() != null) {
                        summary.append("  - Description: ").append(truncateText(feature.getDescription(), 100)).append("\n");
                    }
                }
            });
            summary.append("\n");
        }
        
        if (!issueLinks.isEmpty()) {
            summary.append("### Issues (").append(issueLinks.size()).append(")\n");
            issueLinks.forEach(link -> {
                var issue = issueRepository.findById(link.getEntityId()).orElse(null);
                if (issue != null) {
                    summary.append("- **").append(issue.getTitle()).append("**\n");
                    summary.append("  - Priority: ").append(issue.getPriority()).append("\n");
                    summary.append("  - Status: ").append(issue.getStatus()).append("\n");
                    summary.append("  - Category: ").append(issue.getCategory()).append("\n");
                    if (issue.getDescription() != null) {
                        summary.append("  - Description: ").append(truncateText(issue.getDescription(), 100)).append("\n");
                    }
                }
            });
            summary.append("\n");
        }
        
        if (!hotfixLinks.isEmpty()) {
            summary.append("### Hotfixes (").append(hotfixLinks.size()).append(")\n");
            hotfixLinks.forEach(link -> {
                var hotfix = hotfixRepository.findById(link.getEntityId()).orElse(null);
                if (hotfix != null) {
                    summary.append("- **").append(hotfix.getTitle()).append("**\n");
                    summary.append("  - Status: ").append(hotfix.getStatus()).append("\n");
                    if (hotfix.getReleaseVersion() != null) {
                        summary.append("  - Release Version: ").append(hotfix.getReleaseVersion()).append("\n");
                    }
                    if (hotfix.getDescription() != null) {
                        summary.append("  - Description: ").append(truncateText(hotfix.getDescription(), 100)).append("\n");
                    }
                }
            });
            summary.append("\n");
        }
        
        if (!incidentLinks.isEmpty()) {
            summary.append("### Incidents (").append(incidentLinks.size()).append(")\n");
            incidentLinks.forEach(link -> {
                var incident = incidentRepository.findById(link.getEntityId()).orElse(null);
                if (incident != null) {
                    summary.append("- **").append(incident.getTitle()).append("**\n");
                    summary.append("  - Severity: ").append(incident.getSeverity()).append("\n");
                    summary.append("  - Status: ").append(incident.getStatus()).append("\n");
                    if (incident.getDescription() != null) {
                        summary.append("  - Description: ").append(truncateText(incident.getDescription(), 100)).append("\n");
                    }
                }
            });
            summary.append("\n");
        }

        // Summary statistics
        summary.append("### Summary\n");
        summary.append("- **Total Microservices:** ").append(release.getReleaseMicroservices().size()).append("\n");
        summary.append("- **Total Linked Items:** ").append(release.getReleaseLinks().size()).append("\n");

        return summary.toString();
    }
    
    private String getEntityNameForSummary(com.devportal.domain.enums.ReleaseLinkType entityType, UUID entityId) {
        return switch (entityType) {
            case FEATURE -> featureRepository.findById(entityId).map(f -> f.getName()).orElse("Unknown");
            case INCIDENT -> incidentRepository.findById(entityId).map(i -> i.getTitle()).orElse("Unknown");
            case HOTFIX -> hotfixRepository.findById(entityId).map(h -> h.getTitle()).orElse("Unknown");
            case ISSUE -> issueRepository.findById(entityId).map(i -> i.getTitle()).orElse("Unknown");
        };
    }

    private String generateIncidentSummary(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found"));

        StringBuilder summary = new StringBuilder();
        summary.append("## Incident Summary: ").append(incident.getTitle()).append("\n\n");
        summary.append("**Severity:** ").append(incident.getSeverity()).append("\n");
        summary.append("**Status:** ").append(incident.getStatus()).append("\n\n");

        if (incident.getDescription() != null) {
            summary.append("### Description\n");
            summary.append(truncateText(incident.getDescription(), 500)).append("\n\n");
        }

        // Note: Incident entity doesn't have rootCause/resolution fields
        // Additional details would come from linked items or description

        return summary.toString();
    }

    private String generateHotfixSummary(UUID hotfixId) {
        Hotfix hotfix = hotfixRepository.findById(hotfixId)
                .orElseThrow(() -> new EntityNotFoundException("Hotfix not found"));

        StringBuilder summary = new StringBuilder();
        summary.append("## Hotfix Summary: ").append(hotfix.getTitle()).append("\n\n");
        summary.append("**Status:** ").append(hotfix.getStatus()).append("\n\n");

        if (hotfix.getDescription() != null) {
            summary.append("### Description\n");
            summary.append(truncateText(hotfix.getDescription(), 500)).append("\n\n");
        }

        if (hotfix.getReleaseVersion() != null) {
            summary.append("### Release Version\n");
            summary.append(hotfix.getReleaseVersion()).append("\n");
        }

        return summary.toString();
    }

    // ==================== DUPLICATE DETECTION ====================

    public List<SimilaritySuggestionResponse> findSimilarItems(String entityType, String title, String description) {
        List<SimilaritySuggestionResponse> suggestions = new ArrayList<>();

        switch (entityType.toUpperCase()) {
            case "ISSUE":
                suggestions.addAll(findSimilarIssues(title, description));
                break;
            case "FEATURE":
                suggestions.addAll(findSimilarFeatures(title, description));
                break;
        }

        return suggestions.stream()
                .sorted((a, b) -> b.getSimilarityScore().compareTo(a.getSimilarityScore()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<SimilaritySuggestionResponse> findSimilarIssues(String title, String description) {
        List<Issue> allIssues = issueRepository.findAll();
        List<SimilaritySuggestionResponse> suggestions = new ArrayList<>();

        for (Issue issue : allIssues) {
            double score = calculateSimilarity(title, description, issue.getTitle(), issue.getDescription());
            if (score > 0.3) {
                suggestions.add(SimilaritySuggestionResponse.builder()
                        .similarEntityType("ISSUE")
                        .similarEntityId(issue.getId())
                        .similarEntityName(issue.getTitle())
                        .similarEntityDescription(truncateText(issue.getDescription(), 200))
                        .similarityScore(BigDecimal.valueOf(score))
                        .suggestionReason("Similar title/description detected")
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        return suggestions;
    }

    private List<SimilaritySuggestionResponse> findSimilarFeatures(String title, String description) {
        List<Feature> allFeatures = featureRepository.findAll();
        List<SimilaritySuggestionResponse> suggestions = new ArrayList<>();

        for (Feature feature : allFeatures) {
            double score = calculateSimilarity(title, description, feature.getName(), feature.getDescription());
            if (score > 0.3) {
                suggestions.add(SimilaritySuggestionResponse.builder()
                        .similarEntityType("FEATURE")
                        .similarEntityId(feature.getId())
                        .similarEntityName(feature.getName())
                        .similarEntityDescription(truncateText(feature.getDescription(), 200))
                        .similarityScore(BigDecimal.valueOf(score))
                        .suggestionReason("Similar title/description detected")
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        return suggestions;
    }

    @Transactional
    public void dismissSuggestion(UUID suggestionId) {
        SimilaritySuggestion suggestion = similarityRepository.findById(suggestionId)
                .orElseThrow(() -> new EntityNotFoundException("Suggestion not found"));
        suggestion.setIsDismissed(true);
        similarityRepository.save(suggestion);
    }

    // ==================== RECOMMENDATION ENGINE ====================

    public List<ReleaseRecommendationResponse> getRecommendationsForRelease(UUID releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found"));

        List<ReleaseRecommendationResponse> recommendations = new ArrayList<>();

        recommendations.addAll(recommendMicroservices(release));
        recommendations.addAll(recommendIssues(release));
        recommendations.addAll(recommendHotfixes(release));

        return recommendations.stream()
                .sorted((a, b) -> b.getRecommendationScore().compareTo(a.getRecommendationScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<ReleaseRecommendationResponse> recommendMicroservices(Release release) {
        Set<UUID> linkedMicroserviceIds = release.getReleaseMicroservices().stream()
                .map(rm -> rm.getMicroservice().getId())
                .collect(Collectors.toSet());

        List<Microservice> allMicroservices = microserviceRepository.findAll();
        List<ReleaseRecommendationResponse> recommendations = new ArrayList<>();

        for (Microservice ms : allMicroservices) {
            if (!linkedMicroserviceIds.contains(ms.getId())) {
                double score = 0.5;
                if (ms.getStatus() != null && ms.getStatus().name().equals("IN_PROGRESS")) {
                    score += 0.3;
                }

                if (score > 0.5) {
                    recommendations.add(ReleaseRecommendationResponse.builder()
                            .releaseId(release.getId())
                            .recommendedEntityType("MICROSERVICE")
                            .recommendedEntityId(ms.getId())
                            .recommendedEntityName(ms.getName())
                            .recommendedEntityDescription(truncateText(ms.getDescription(), 200))
                            .recommendationScore(BigDecimal.valueOf(score))
                            .recommendationReason("Active microservice with recent changes")
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }
        }

        return recommendations;
    }

    private List<ReleaseRecommendationResponse> recommendIssues(Release release) {
        Set<UUID> linkedIssueIds = release.getReleaseLinks().stream()
                .filter(link -> "ISSUE".equals(link.getEntityType().name()))
                .map(ReleaseLink::getEntityId)
                .collect(Collectors.toSet());

        List<Issue> resolvedIssues = issueRepository.findAll().stream()
                .filter(i -> "RESOLVED".equals(i.getStatus().name()) || "CLOSED".equals(i.getStatus().name()))
                .filter(i -> !linkedIssueIds.contains(i.getId()))
                .collect(Collectors.toList());

        List<ReleaseRecommendationResponse> recommendations = new ArrayList<>();

        for (Issue issue : resolvedIssues) {
            double score = 0.6;
            if ("HIGH".equals(issue.getPriority().name())) {
                score += 0.2;
            }

            recommendations.add(ReleaseRecommendationResponse.builder()
                    .releaseId(release.getId())
                    .recommendedEntityType("ISSUE")
                    .recommendedEntityId(issue.getId())
                    .recommendedEntityName(issue.getTitle())
                    .recommendedEntityDescription(truncateText(issue.getDescription(), 200))
                    .recommendationScore(BigDecimal.valueOf(score))
                    .recommendationReason("Resolved issue ready for release")
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return recommendations;
    }

    private List<ReleaseRecommendationResponse> recommendHotfixes(Release release) {
        Set<UUID> linkedHotfixIds = release.getReleaseLinks().stream()
                .filter(link -> "HOTFIX".equals(link.getEntityType().name()))
                .map(ReleaseLink::getEntityId)
                .collect(Collectors.toSet());

        List<Hotfix> completedHotfixes = hotfixRepository.findAll().stream()
                .filter(h -> "DEPLOYED".equals(h.getStatus().name()) || "VERIFIED".equals(h.getStatus().name()))
                .filter(h -> !linkedHotfixIds.contains(h.getId()))
                .collect(Collectors.toList());

        List<ReleaseRecommendationResponse> recommendations = new ArrayList<>();

        for (Hotfix hotfix : completedHotfixes) {
            recommendations.add(ReleaseRecommendationResponse.builder()
                    .releaseId(release.getId())
                    .recommendedEntityType("HOTFIX")
                    .recommendedEntityId(hotfix.getId())
                    .recommendedEntityName(hotfix.getTitle())
                    .recommendedEntityDescription(truncateText(hotfix.getDescription(), 200))
                    .recommendationScore(BigDecimal.valueOf(0.75))
                    .recommendationReason("Deployed hotfix ready for inclusion")
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return recommendations;
    }

    @Transactional
    public void acceptRecommendation(UUID recommendationId) {
        ReleaseRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
        recommendation.setIsAccepted(true);
        recommendationRepository.save(recommendation);
    }

    @Transactional
    public void dismissRecommendation(UUID recommendationId) {
        ReleaseRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
        recommendation.setIsDismissed(true);
        recommendationRepository.save(recommendation);
    }

    // ==================== HELPER METHODS ====================

    private double calculateSimilarity(String title1, String desc1, String title2, String desc2) {
        double titleSimilarity = calculateJaccardSimilarity(
                tokenize(title1 != null ? title1 : ""),
                tokenize(title2 != null ? title2 : "")
        );

        double descSimilarity = calculateJaccardSimilarity(
                tokenize(desc1 != null ? desc1 : ""),
                tokenize(desc2 != null ? desc2 : "")
        );

        return (titleSimilarity * 0.6) + (descSimilarity * 0.4);
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toSet());
    }

    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return null;
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private AiSummaryResponse mapSummaryToResponse(AiSummary summary) {
        return AiSummaryResponse.builder()
                .id(summary.getId())
                .entityType(summary.getEntityType())
                .entityId(summary.getEntityId())
                .summaryType(summary.getSummaryType())
                .summaryText(summary.getSummaryText())
                .generatedAt(summary.getGeneratedAt())
                .generatedBy(summary.getGeneratedBy())
                .isApproved(summary.getIsApproved())
                .approvedBy(summary.getApprovedBy() != null ? UserSummary.builder()
                        .id(summary.getApprovedBy().getId())
                        .username(summary.getApprovedBy().getUsername())
                        .build() : null)
                .approvedAt(summary.getApprovedAt())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
