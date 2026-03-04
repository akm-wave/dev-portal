package com.devportal.dto.response;

import com.devportal.domain.enums.ReleaseLinkType;
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
public class ReleaseLinkResponse {

    private UUID id;
    private ReleaseLinkType entityType;
    private UUID entityId;
    private String entityName;
    private LocalDateTime createdAt;
}
