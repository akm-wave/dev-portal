package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityCategoryResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private String parentName;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private Integer utilityCount;
    private List<UtilityCategoryResponse> children;
}
