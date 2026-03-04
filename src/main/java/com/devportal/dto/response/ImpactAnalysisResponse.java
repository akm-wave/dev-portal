package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactAnalysisResponse {
    private UUID id;
    private UUID featureId;
    private UUID incidentId;
    private UUID hotfixId;
    private UUID issueId;
    private int riskScore;
    private String riskLevel;
    private List<ImpactedArea> impactedAreas;
    private List<ImpactedMicroservice> impactedMicroservices;
    private List<CriticalChecklist> criticalChecklists;
    private List<RecommendedTest> recommendedTests;
    private String analysisSummary;
    private String createdBy;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactedArea {
        private String name;
        private String domain;
        private int impactLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactedMicroservice {
        private UUID id;
        private String name;
        private List<String> changeTypes;
        private int riskScore;
        private String riskColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriticalChecklist {
        private UUID id;
        private String name;
        private String priority;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedTest {
        private String testType;
        private String description;
        private String microserviceName;
        private String priority;
    }
}
