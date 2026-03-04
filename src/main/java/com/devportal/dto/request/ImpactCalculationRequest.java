package com.devportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactCalculationRequest {
    private UUID featureId;
    private UUID incidentId;
    private UUID hotfixId;
    private UUID issueId;
    private List<MicroserviceChangeRequest> microserviceChanges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroserviceChangeRequest {
        private UUID microserviceId;
        private List<String> changeTypes;
        private String notes;
    }
}
