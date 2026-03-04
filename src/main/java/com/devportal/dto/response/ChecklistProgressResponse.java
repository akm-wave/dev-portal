package com.devportal.dto.response;

import com.devportal.domain.enums.ChecklistStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChecklistProgressResponse {
    private UUID id;
    private UUID checklistId;
    private String checklistName;
    private String checklistDescription;
    private ChecklistStatus status;
    private String remark;
    private String mongoFileId;
    private String attachmentFilename;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
