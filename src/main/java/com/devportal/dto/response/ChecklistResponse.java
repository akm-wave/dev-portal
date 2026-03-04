package com.devportal.dto.response;

import com.devportal.domain.enums.ChecklistPriority;
import com.devportal.domain.enums.ChecklistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistResponse {
    private UUID id;
    private String name;
    private String description;
    private ChecklistStatus status;
    private ChecklistPriority priority;
    private String createdBy;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
