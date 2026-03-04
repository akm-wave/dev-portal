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
public class UserNoteResponse {

    private UUID id;
    private String title;
    private String description;
    private List<String> tags;
    private Boolean isPinned;
    private Boolean isArchived;
    private String moduleType;
    private UUID moduleId;
    private String moduleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
