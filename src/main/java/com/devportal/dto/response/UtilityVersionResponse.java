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
public class UtilityVersionResponse {

    private UUID id;
    private UUID utilityId;
    private Integer versionNumber;
    private String title;
    private String description;
    private String content;
    private String changeSummary;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private Boolean isCurrent;
}
