package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseRecommendationResponse {

    private UUID id;
    private UUID releaseId;
    private String recommendedEntityType;
    private UUID recommendedEntityId;
    private String recommendedEntityName;
    private String recommendedEntityDescription;
    private BigDecimal recommendationScore;
    private String recommendationReason;
    private Boolean isAccepted;
    private LocalDateTime createdAt;
}
