package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalFeatures;
    private long totalMicroservices;
    private long totalChecklists;
    
    private Map<String, Long> featuresByStatus;
    private Map<String, Long> microservicesByStatus;
    private Map<String, Long> checklistsByStatus;
    
    private double overallProgress;
    
    private List<ActivityLogResponse> recentActivities;
    
    private List<HighImpactService> highImpactServices;
    private List<TechnicalDebtService> technicalDebtServices;
    
    private Map<String, Long> issuesByCategory;
    private long totalTechDebtIssues;
    private long openTechDebtIssues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighImpactService {
        private String id;
        private String name;
        private int featureCount;
        private double progressPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalDebtService {
        private String id;
        private String name;
        private int debtScore;
        private int blockedCount;
        private int stalePendingCount;
    }
}
