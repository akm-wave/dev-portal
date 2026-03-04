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
public class UtilityAttachmentResponse {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private String mongoFileId;
    private String fileType;
    private Long fileSize;
    private UserSummary uploadedBy;
    private LocalDateTime uploadedAt;
}
