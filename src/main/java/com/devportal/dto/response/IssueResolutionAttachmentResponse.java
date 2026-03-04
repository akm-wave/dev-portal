package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResolutionAttachmentResponse {
    private String id;
    private String mongoFileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private UserSummary uploadedBy;
    private LocalDateTime uploadedAt;
}
