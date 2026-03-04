package com.devportal.dto.response;

import com.devportal.domain.enums.UtilityType;
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
public class UtilityResponse {
    private UUID id;
    private String title;
    private UtilityType type;
    private String description;
    private String version;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UtilityAttachmentResponse> attachments;
    private int attachmentCount;
}
