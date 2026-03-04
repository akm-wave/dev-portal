package com.devportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private UUID parentId;

    private Integer sortOrder;
}
