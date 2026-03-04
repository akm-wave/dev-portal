package com.devportal.dto.response;

import com.devportal.domain.enums.IssueCategory;
import com.devportal.domain.enums.IssuePriority;
import com.devportal.domain.enums.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private UUID id;
    private String title;
    private String description;
    private IssuePriority priority;
    private IssueStatus status;
    private IssueCategory category;
    private IncidentResponse.FeatureSummary mainFeature;
    private IncidentResponse.UserSummary assignedTo;
    private IncidentResponse.UserSummary owner;
    private String createdBy;
    private String resultComment;
    private String attachmentUrl;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
