package com.devportal.dto.response;

import com.devportal.domain.enums.FeatureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResponse {
    private UUID id;
    private String name;
    private String description;
    private String domain;
    private String releaseVersion;
    private LocalDate targetDate;
    private FeatureStatus status;
    private List<MicroserviceResponse> microservices;
    private int microserviceCount;
    private int totalChecklistCount;
    private int completedChecklistCount;
    private double progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummary owner;
}
