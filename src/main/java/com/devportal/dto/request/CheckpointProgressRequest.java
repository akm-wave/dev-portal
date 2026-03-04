package com.devportal.dto.request;

import com.devportal.domain.enums.ChecklistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointProgressRequest {
    private ChecklistStatus status;
    private String remark;
    private String attachmentUrl;
}
