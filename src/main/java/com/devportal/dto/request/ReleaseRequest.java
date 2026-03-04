package com.devportal.dto.request;

import com.devportal.domain.enums.ReleaseStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseRequest {

    @NotBlank(message = "Release name is required")
    private String name;

    @NotBlank(message = "Release version is required")
    private String version;

    private LocalDateTime releaseDate;

    private String description;

    private ReleaseStatus status;

    private String oldBuildNumber;

    private String featureBranch;

    private List<ReleaseMicroserviceRequest> microservices;

    private List<ReleaseLinkRequest> links;
}
