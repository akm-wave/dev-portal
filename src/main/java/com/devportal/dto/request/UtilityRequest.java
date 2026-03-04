package com.devportal.dto.request;

import com.devportal.domain.enums.UtilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title must not exceed 300 characters")
    private String title;

    private UtilityType type;

    private String description;

    private String content;

    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;
}
