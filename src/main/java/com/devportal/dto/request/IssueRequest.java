package com.devportal.dto.request;

import com.devportal.domain.enums.IssueCategory;
import com.devportal.domain.enums.IssuePriority;
import com.devportal.domain.enums.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private IssuePriority priority;

    private IssueStatus status;

    private IssueCategory category;

    @NotNull(message = "Main Feature is required")
    private UUID mainFeatureId;

    private UUID assignedToId;

    private UUID ownerId;

    private String resultComment;

    private String attachmentUrl;
}
