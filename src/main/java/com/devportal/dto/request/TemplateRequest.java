package com.devportal.dto.request;

import com.devportal.domain.enums.TemplateEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotNull(message = "Entity type is required")
    private TemplateEntityType entityType;

    @NotNull(message = "Template data is required")
    private Map<String, Object> templateData;

    private Boolean isDefault;
}
