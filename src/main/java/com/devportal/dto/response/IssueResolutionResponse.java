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
public class IssueResolutionResponse {
    private String id;
    private String issueId;
    private String comment;
    private Boolean isResolutionComment;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<IssueResolutionAttachmentResponse> attachments;
}
