package com.devportal.dto.response;

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
public class IssueCommentResponse {
    private UUID id;
    private String content;
    private UserSummary user;
    private Boolean isResolutionComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
