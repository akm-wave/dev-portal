package com.devportal.dto.request;

import com.devportal.domain.enums.ReleaseLinkType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseLinkRequest {

    @NotNull(message = "Entity type is required")
    private ReleaseLinkType entityType;

    @NotNull(message = "Entity ID is required")
    private UUID entityId;
}
