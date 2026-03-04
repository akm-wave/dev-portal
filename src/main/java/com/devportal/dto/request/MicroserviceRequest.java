package com.devportal.dto.request;

import com.devportal.domain.enums.MicroserviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroserviceRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    private String description;
    
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;
    
    @Size(max = 100, message = "Owner must not exceed 100 characters")
    private String owner;
    
    private MicroserviceStatus status;
    
    @NotEmpty(message = "At least one checklist is required")
    private List<UUID> checklistIds;

    @Size(max = 500, message = "GitLab URL must not exceed 500 characters")
    private String gitlabUrl;
}
