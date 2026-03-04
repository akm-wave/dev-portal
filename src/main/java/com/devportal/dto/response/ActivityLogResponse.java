package com.devportal.dto.response;

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
public class ActivityLogResponse {
    private UUID id;
    private String username;
    private String action;
    private String entityType;
    private UUID entityId;
    private String description;
    private String ipAddress;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
