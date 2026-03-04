package com.devportal.dto.response;

import com.devportal.domain.enums.MicroserviceStatus;
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
public class MicroserviceResponse {
    private UUID id;
    private String name;
    private String description;
    private String version;
    private UserSummary owner;
    private MicroserviceStatus status;
    private List<ChecklistResponse> checklists;
    private int checklistCount;
    private int completedChecklistCount;
    private double progressPercentage;
    private Boolean highRisk;
    private Integer technicalDebtScore;
    private int featureCount;
    private String gitlabUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
