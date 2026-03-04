package com.devportal.dto.request;

import com.devportal.domain.enums.ChecklistStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChecklistProgressUpdateRequest {
    
    @NotNull(message = "Status is required")
    private ChecklistStatus status;
    
    private String remark;
}
