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
public class FeatureCheckpointResponse {
    private UUID id;
    private UUID featureId;
    private UUID checklistId;
    private String checklistName;
    private String checklistDescription;
    private String checklistPriority;
    private String status;
    private String remark;
    private String attachmentUrl;
    private String mongoFileId;
    private String attachmentFilename;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
