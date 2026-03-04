package com.devportal.dto.response;

import com.devportal.domain.enums.IncidentStatus;
import com.devportal.domain.enums.Severity;
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
public class IncidentResponse {
    private UUID id;
    private String title;
    private String description;
    private Severity severity;
    private IncidentStatus status;
    private FeatureSummary mainFeature;
    private UserSummary owner;
    private String createdBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MicroserviceSummary> microservices;
    private int microserviceCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureSummary {
        private UUID id;
        private String name;
        private String domain;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private UUID id;
        private String username;
        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroserviceSummary {
        private UUID id;
        private String name;
        private String status;
    }
}
