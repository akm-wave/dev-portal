package com.devportal.dto.request;

import com.devportal.domain.enums.IncidentStatus;
import com.devportal.domain.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;

    private IncidentStatus status;

    @NotNull(message = "Main Feature is required")
    private UUID mainFeatureId;

    private UUID ownerId;

    private List<UUID> microserviceIds;
}
