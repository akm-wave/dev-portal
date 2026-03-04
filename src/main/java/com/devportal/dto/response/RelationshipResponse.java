package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipResponse {
    private List<MicroserviceNode> microservices;
    private List<FeatureNode> features;
    private List<RelationshipEdge> relationships;
    private Map<String, Set<String>> microserviceToFeatures;
    private Map<String, Set<String>> featureToMicroservices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroserviceNode {
        private String id;
        private String name;
        private String description;
        private String status;
        private String owner;
        private String version;
        private int featureCount;
        private int checklistCount;
        private int completedChecklistCount;
        private double progressPercentage;
        private boolean highRisk;
        private int technicalDebtScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureNode {
        private String id;
        private String name;
        private String description;
        private String domain;
        private String status;
        private String releaseVersion;
        private String targetDate;
        private int microserviceCount;
        private boolean isShared;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipEdge {
        private String microserviceId;
        private String featureId;
    }
}
