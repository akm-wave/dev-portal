package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceProductivityDTO {
    
    // SECTION A - My Active Work
    private Long activeFeatures;
    private Long activeIncidents;
    private Long activeHotfixes;
    private Long activeIssues;
    private Long activeMicroservices;
    private Long overdueTasks;
    
    // SECTION B - My Productivity Metrics
    private Double completionRate;
    private Double onTimeRate;
    private Double avgResolutionTime; // in hours
    private Integer productivityScore;
    private Integer previousPeriodScore;
    private Double trendPercentage; // positive for improvement, negative for decline
    private List<WeekScoreDTO> weeklyTrend;
    
    // SECTION C - Accountability
    private List<OverdueTaskDTO> overdueItems;
    private List<ActivityDTO> recentActivities;
    
    // Metadata
    private LocalDateTime generatedAt;
    private String dateRange;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekScoreDTO {
        private String weekStart; // ISO date string
        private String weekEnd;   // ISO date string
        private Integer score;
        private Integer completedItems;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverdueTaskDTO {
        private String id;
        private String title;
        private String type; // FEATURE, INCIDENT, HOTFIX, ISSUE, CHECKPOINT
        private String status;
        private LocalDateTime dueDate;
        private LocalDateTime createdAt;
        private Integer daysOverdue;
        private String priority;
        private String assignee;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDTO {
        private String id;
        private String type; // COMPLETED_FEATURE, RESOLVED_ISSUE, RESOLVED_INCIDENT, DEPLOYED_HOTFIX, COMPLETED_CHECKPOINT
        private String title;
        private LocalDateTime completedAt;
        private LocalDateTime dueDate;
        private boolean onTime;
        private Integer points;
        private String assignee;
    }
}
