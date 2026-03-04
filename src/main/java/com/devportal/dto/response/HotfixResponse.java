package com.devportal.dto.response;

import com.devportal.domain.enums.HotfixStatus;
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
public class HotfixResponse {
    private UUID id;
    private String title;
    private String description;
    private String releaseVersion;
    private HotfixStatus status;
    private IncidentResponse.FeatureSummary mainFeature;
    private IncidentResponse.UserSummary owner;
    private String createdBy;
    private LocalDateTime deployedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<IncidentResponse.MicroserviceSummary> microservices;
    private int microserviceCount;
}
