package com.devportal.controller;

import com.devportal.dto.request.IssueCommentRequest;
import com.devportal.dto.request.IssueRequest;
import com.devportal.dto.request.IssueResolutionRequest;
import com.devportal.dto.response.*;
import com.devportal.service.IssueResolutionService;
import com.devportal.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Issue management APIs")
public class IssueController {

    private final IssueService issueService;
    private final IssueResolutionService resolutionService;

    @GetMapping
    @Operation(summary = "Get all issues")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getAll(Pageable pageable) {
        Page<IssueResponse> issues = issueService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Issues retrieved successfully", issues));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID")
    public ResponseEntity<ApiResponse<IssueResponse>> getById(@PathVariable UUID id) {
        IssueResponse issue = issueService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Issue retrieved successfully", issue));
    }

    @GetMapping("/assigned/{userId}")
    @Operation(summary = "Get issues assigned to a user")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getByAssignedUser(@PathVariable UUID userId) {
        List<IssueResponse> issues = issueService.getByAssignedUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Issues retrieved successfully", issues));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new issue")
    public ResponseEntity<ApiResponse<IssueResponse>> create(@Valid @RequestBody IssueRequest request) {
        IssueResponse issue = issueService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Issue created successfully", issue));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an issue")
    public ResponseEntity<ApiResponse<IssueResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody IssueRequest request) {
        IssueResponse issue = issueService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Issue updated successfully", issue));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign issue to a user")
    public ResponseEntity<ApiResponse<IssueResponse>> assignIssue(
            @PathVariable UUID id,
            @RequestBody Map<String, UUID> body) {
        IssueResponse issue = issueService.assignIssue(id, body.get("userId"));
        return ResponseEntity.ok(ApiResponse.success("Issue assigned successfully", issue));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolve an issue with comment and attachment")
    public ResponseEntity<ApiResponse<IssueResponse>> resolveIssue(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        IssueResponse issue = issueService.resolveIssue(id, body.get("resultComment"), body.get("attachmentUrl"));
        return ResponseEntity.ok(ApiResponse.success("Issue resolved successfully", issue));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an issue")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        issueService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Issue deleted successfully", null));
    }

    // ============== Resolution Endpoints ==============

    @GetMapping("/{id}/attachments")
    @Operation(summary = "Get all attachments for an issue")
    public ResponseEntity<ApiResponse<List<IssueAttachmentResponse>>> getAttachments(@PathVariable UUID id) {
        List<IssueAttachmentResponse> attachments = resolutionService.getAttachments(id);
        return ResponseEntity.ok(ApiResponse.success("Attachments retrieved successfully", attachments));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Upload attachment to an issue (owner only)")
    public ResponseEntity<ApiResponse<IssueAttachmentResponse>> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        IssueAttachmentResponse attachment = resolutionService.uploadAttachment(id, file);
        return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully", attachment));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @Operation(summary = "Delete an attachment (owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable UUID id,
            @PathVariable UUID attachmentId) {
        resolutionService.deleteAttachment(id, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get all comments for an issue")
    public ResponseEntity<ApiResponse<List<IssueCommentResponse>>> getComments(@PathVariable UUID id) {
        List<IssueCommentResponse> comments = resolutionService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to an issue")
    public ResponseEntity<ApiResponse<IssueCommentResponse>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody IssueCommentRequest request) {
        IssueCommentResponse comment = resolutionService.addComment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId) {
        resolutionService.deleteComment(id, commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }

    @GetMapping("/{id}/is-owner")
    @Operation(summary = "Check if current user is the issue owner")
    public ResponseEntity<ApiResponse<Boolean>> isOwner(@PathVariable UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = resolutionService.isOwner(id, username);
        return ResponseEntity.ok(ApiResponse.success("Owner check completed", isOwner));
    }

    // ============== Resolution Timeline Endpoints ==============

    @GetMapping("/{id}/resolutions")
    @Operation(summary = "Get resolution timeline for an issue (paginated)")
    public ResponseEntity<ApiResponse<Page<IssueResolutionResponse>>> getResolutions(
            @PathVariable UUID id,
            Pageable pageable) {
        Page<IssueResolutionResponse> resolutions = resolutionService.getResolutions(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Resolutions retrieved successfully", resolutions));
    }

    @GetMapping("/{id}/resolutions/all")
    @Operation(summary = "Get all resolutions for an issue")
    public ResponseEntity<ApiResponse<List<IssueResolutionResponse>>> getAllResolutions(@PathVariable UUID id) {
        List<IssueResolutionResponse> resolutions = resolutionService.getAllResolutions(id);
        return ResponseEntity.ok(ApiResponse.success("Resolutions retrieved successfully", resolutions));
    }

    @PostMapping("/{id}/resolutions")
    @Operation(summary = "Create a resolution entry with optional attachments")
    public ResponseEntity<ApiResponse<IssueResolutionResponse>> createResolution(
            @PathVariable UUID id,
            @RequestPart(value = "data", required = false) IssueResolutionRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        if (request == null) {
            request = new IssueResolutionRequest();
        }
        IssueResolutionResponse resolution = resolutionService.createResolution(id, request, files);
        return ResponseEntity.ok(ApiResponse.success("Resolution created successfully", resolution));
    }

    @PostMapping("/{id}/resolutions/{resolutionId}/attachments")
    @Operation(summary = "Add attachment to a resolution")
    public ResponseEntity<ApiResponse<IssueResolutionAttachmentResponse>> addResolutionAttachment(
            @PathVariable UUID id,
            @PathVariable UUID resolutionId,
            @RequestParam("file") MultipartFile file) throws IOException {
        IssueResolutionAttachmentResponse attachment = resolutionService.addResolutionAttachment(id, resolutionId, file);
        return ResponseEntity.ok(ApiResponse.success("Attachment added successfully", attachment));
    }

    @DeleteMapping("/{id}/resolutions/{resolutionId}/attachments/{attachmentId}")
    @Operation(summary = "Delete a resolution attachment")
    public ResponseEntity<ApiResponse<Void>> deleteResolutionAttachment(
            @PathVariable UUID id,
            @PathVariable UUID resolutionId,
            @PathVariable UUID attachmentId) {
        resolutionService.deleteResolutionAttachment(id, resolutionId, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
