package com.devportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueCommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
    
    private Boolean isResolutionComment = false;
}
