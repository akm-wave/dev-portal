package com.devportal.dto.request;

import com.devportal.domain.enums.FeatureStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Domain is required")
    @Size(max = 50, message = "Domain must not exceed 50 characters")
    private String domain;
    
    @Size(max = 50, message = "Release version must not exceed 50 characters")
    private String releaseVersion;
    
    private LocalDate targetDate;
    
    private FeatureStatus status;
    
    @NotEmpty(message = "At least one microservice is required")
    private List<UUID> microserviceIds;
    
    private UUID ownerId;
}
