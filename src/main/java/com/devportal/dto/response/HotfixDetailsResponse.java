package com.devportal.dto.response;

import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.domain.enums.HotfixStatus;
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
public class HotfixDetailsResponse {
    private String id;
    private String title;
    private String description;
    private HotfixStatus status;
    private String createdBy;
    private LocalDateTime deployedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private FeatureSummary mainFeature;
    private UserSummary owner;

    private int totalMicroservices;
    private int totalUniqueCheckpoints;
    private double overallProgress;

    private List<MicroserviceAnalysis> microservices;
    private List<CheckpointAnalysis> checkpoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureSummary {
        private String id;
        private String name;
        private String domain;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private String id;
        private String username;
        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroserviceAnalysis {
        private String id;
        private String name;
        private String description;
        private String status;
        private String owner;
        private String version;
        private double progressPercentage;
        private int totalCheckpoints;
        private int completedCheckpoints;
        private boolean highRisk;
        private List<CheckpointSummary> checkpoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckpointAnalysis {
        private String id;
        private String name;
        private String description;
        private ChecklistStatus originalStatus;
        private ChecklistStatus hotfixStatus;
        private String priority;
        private String remark;
        private String mongoFileId;
        private String attachmentFilename;
        private String updatedBy;
        private LocalDateTime updatedAt;
        private List<String> connectedMicroservices;
        private List<String> connectedMicroserviceIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckpointSummary {
        private String id;
        private String name;
        private ChecklistStatus status;
        private String priority;
    }
}
