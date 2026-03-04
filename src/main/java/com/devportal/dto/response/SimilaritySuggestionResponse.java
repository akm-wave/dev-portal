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
public class SimilaritySuggestionResponse {

    private UUID id;
    private String sourceEntityType;
    private UUID sourceEntityId;
    private String similarEntityType;
    private UUID similarEntityId;
    private String similarEntityName;
    private String similarEntityDescription;
    private BigDecimal similarityScore;
    private String suggestionReason;
    private LocalDateTime createdAt;
}
