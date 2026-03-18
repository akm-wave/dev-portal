package com.devportal.controller;

import com.devportal.dto.request.ChecklistProgressUpdateRequest;
import com.devportal.dto.request.HotfixRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.ChecklistProgressResponse;
import com.devportal.dto.response.FileDownloadResponse;
import com.devportal.dto.response.HotfixDetailsResponse;
import com.devportal.dto.response.HotfixResponse;
import com.devportal.service.GridFsFileService;
import com.devportal.service.HotfixChecklistService;
import com.devportal.service.HotfixDetailsService;
import com.devportal.service.HotfixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.devportal.domain.enums.HotfixStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/hotfixes")
@RequiredArgsConstructor
@Tag(name = "Hotfixes", description = "Hotfix management APIs")
public class HotfixController {

    private final HotfixService hotfixService;
    private final HotfixChecklistService checklistService;
    private final HotfixDetailsService detailsService;
    private final GridFsFileService gridFsFileService;

    @GetMapping
    @Operation(summary = "Get all hotfixes")
    public ResponseEntity<ApiResponse<Page<HotfixResponse>>> getAll(
            Pageable pageable,
            @RequestParam(required = false) HotfixStatus status,
            @RequestParam(required = false) String search) {
        Page<HotfixResponse> hotfixes = hotfixService.getAll(pageable, status, search);
        return ResponseEntity.ok(ApiResponse.success("Hotfixes retrieved successfully", hotfixes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotfix by ID")
    public ResponseEntity<ApiResponse<HotfixResponse>> getById(@PathVariable UUID id) {
        HotfixResponse hotfix = hotfixService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Hotfix retrieved successfully", hotfix));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get hotfix details with checkpoint analysis")
    public ResponseEntity<ApiResponse<HotfixDetailsResponse>> getDetails(@PathVariable UUID id) {
        HotfixDetailsResponse details = detailsService.getHotfixDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Hotfix details retrieved successfully", details));
    }

    @PostMapping
    @Operation(summary = "Create a new hotfix")
    public ResponseEntity<ApiResponse<HotfixResponse>> create(@Valid @RequestBody HotfixRequest request) {
        HotfixResponse hotfix = hotfixService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Hotfix created successfully", hotfix));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a hotfix")
    public ResponseEntity<ApiResponse<HotfixResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HotfixRequest request) {
        HotfixResponse hotfix = hotfixService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hotfix updated successfully", hotfix));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a hotfix")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        hotfixService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Hotfix deleted successfully", null));
    }

    // Checklist Progress Endpoints
    @GetMapping("/{id}/checklists")
    @Operation(summary = "Get checklist progress for a hotfix")
    public ResponseEntity<ApiResponse<List<ChecklistProgressResponse>>> getChecklistProgress(@PathVariable UUID id) {
        List<ChecklistProgressResponse> progress = checklistService.getChecklistProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Checklist progress retrieved successfully", progress));
    }

    @PutMapping("/{id}/checklists/{checklistId}")
    @Operation(summary = "Update checklist status for a hotfix")
    public ResponseEntity<ApiResponse<ChecklistProgressResponse>> updateChecklistStatus(
            @PathVariable UUID id,
            @PathVariable UUID checklistId,
            @Valid @RequestBody ChecklistProgressUpdateRequest request) {
        ChecklistProgressResponse progress = checklistService.updateChecklistStatus(id, checklistId, request);
        return ResponseEntity.ok(ApiResponse.success("Checklist status updated successfully", progress));
    }

    @PostMapping("/{id}/checklists/{checklistId}/attachment")
    @Operation(summary = "Upload attachment for a checklist")
    public ResponseEntity<ApiResponse<ChecklistProgressResponse>> uploadChecklistAttachment(
            @PathVariable UUID id,
            @PathVariable UUID checklistId,
            @RequestParam("file") MultipartFile file) throws IOException {
        ChecklistProgressResponse progress = checklistService.uploadAttachment(id, checklistId, file);
        return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully", progress));
    }

    @GetMapping("/{id}/checklists/{checklistId}/attachment")
    @Operation(summary = "Download attachment for a checklist")
    public ResponseEntity<byte[]> downloadChecklistAttachment(
            @PathVariable UUID id,
            @PathVariable UUID checklistId) {
        List<ChecklistProgressResponse> progressList = checklistService.getChecklistProgress(id);
        ChecklistProgressResponse progress = progressList.stream()
                .filter(p -> p.getChecklistId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        
        if (progress.getMongoFileId() == null) {
            throw new RuntimeException("No attachment found");
        }
        
        FileDownloadResponse file = gridFsFileService.downloadFile(progress.getMongoFileId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getData());
    }

    @DeleteMapping("/{id}/checklists/{checklistId}/attachment")
    @Operation(summary = "Delete attachment for a checklist")
    public ResponseEntity<ApiResponse<Void>> deleteChecklistAttachment(
            @PathVariable UUID id,
            @PathVariable UUID checklistId) {
        checklistService.deleteAttachment(id, checklistId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
