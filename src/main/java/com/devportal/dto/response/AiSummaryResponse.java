package com.devportal.dto.response;

import com.devportal.domain.enums.SummaryType;
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
public class AiSummaryResponse {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private SummaryType summaryType;
    private String summaryText;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private Boolean isApproved;
    private UserSummary approvedBy;
    private LocalDateTime approvedAt;
}
