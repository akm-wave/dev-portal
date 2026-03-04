package com.devportal.dto.response;

import com.devportal.domain.enums.ReleaseStatus;
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
public class ReleaseResponse {

    private UUID id;
    private String name;
    private String version;
    private LocalDateTime releaseDate;
    private String description;
    private ReleaseStatus status;
    private String oldBuildNumber;
    private String featureBranch;
    private UserSummary createdBy;
    private List<ReleaseMicroserviceResponse> microservices;
    private List<ReleaseLinkResponse> links;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
