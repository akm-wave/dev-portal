package com.devportal.dto.response;

import com.devportal.domain.enums.TemplateEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {

    private UUID id;
    private String name;
    private String description;
    private TemplateEntityType entityType;
    private Map<String, Object> templateData;
    private Boolean isDefault;
    private Boolean isActive;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
