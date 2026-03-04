package com.devportal.controller;

import com.devportal.domain.enums.UtilityType;
import com.devportal.dto.request.UtilityRequest;
import com.devportal.dto.response.*;
import com.devportal.service.UtilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/utilities")
@RequiredArgsConstructor
@Tag(name = "Utilities", description = "Utility document management APIs")
public class UtilityController {

    private final UtilityService utilityService;

    @GetMapping
    @Operation(summary = "Get all utilities with pagination, search, and filter")
    public ResponseEntity<ApiResponse<PagedResponse<UtilityResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UtilityType type,
            @RequestParam(required = false) String search) {
        PagedResponse<UtilityResponse> utilities = utilityService.getAll(page, size, sortBy, sortDir, type, search);
        return ResponseEntity.ok(ApiResponse.success("Utilities retrieved successfully", utilities));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get utility by ID")
    public ResponseEntity<ApiResponse<UtilityResponse>> getById(@PathVariable UUID id) {
        UtilityResponse utility = utilityService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Utility retrieved successfully", utility));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new utility")
    public ResponseEntity<ApiResponse<UtilityResponse>> create(@Valid @RequestBody UtilityRequest request) {
        UtilityResponse utility = utilityService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Utility created successfully", utility));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a utility")
    public ResponseEntity<ApiResponse<UtilityResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UtilityRequest request) {
        UtilityResponse utility = utilityService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utility updated successfully", utility));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a utility")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        utilityService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Utility deleted successfully", null));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "Get all attachments for a utility")
    public ResponseEntity<ApiResponse<List<UtilityAttachmentResponse>>> getAttachments(@PathVariable UUID id) {
        List<UtilityAttachmentResponse> attachments = utilityService.getAttachments(id);
        return ResponseEntity.ok(ApiResponse.success("Attachments retrieved successfully", attachments));
    }

    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload attachment to a utility")
    public ResponseEntity<ApiResponse<UtilityAttachmentResponse>> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        UtilityAttachmentResponse attachment = utilityService.uploadAttachment(id, file);
        return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully", attachment));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable UUID id,
            @PathVariable UUID attachmentId) {
        utilityService.deleteAttachment(id, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
