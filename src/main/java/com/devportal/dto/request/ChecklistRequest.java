package com.devportal.dto.request;

import com.devportal.domain.enums.ChecklistPriority;
import com.devportal.domain.enums.ChecklistStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    private String description;
    
    private ChecklistStatus status;
    
    private ChecklistPriority priority;
}
