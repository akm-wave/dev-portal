package com.devportal.controller;

import com.devportal.dto.request.ChecklistProgressUpdateRequest;
import com.devportal.dto.request.IncidentRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.ChecklistProgressResponse;
import com.devportal.dto.response.FileDownloadResponse;
import com.devportal.dto.response.IncidentDetailsResponse;
import com.devportal.dto.response.IncidentResponse;
import com.devportal.service.GridFsFileService;
import com.devportal.service.IncidentChecklistService;
import com.devportal.service.IncidentDetailsService;
import com.devportal.service.IncidentService;
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

import com.devportal.domain.enums.IncidentStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident management APIs")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentChecklistService checklistService;
    private final IncidentDetailsService detailsService;
    private final GridFsFileService gridFsFileService;

    @GetMapping
    @Operation(summary = "Get all incidents")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getAll(
            Pageable pageable,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) String search) {
        Page<IncidentResponse> incidents = incidentService.getAll(pageable, status, search);
        return ResponseEntity.ok(ApiResponse.success("Incidents retrieved successfully", incidents));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<ApiResponse<IncidentResponse>> getById(@PathVariable UUID id) {
        IncidentResponse incident = incidentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Incident retrieved successfully", incident));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get incident details with checkpoint analysis")
    public ResponseEntity<ApiResponse<IncidentDetailsResponse>> getDetails(@PathVariable UUID id) {
        IncidentDetailsResponse details = detailsService.getIncidentDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Incident details retrieved successfully", details));
    }

    @PostMapping
    @Operation(summary = "Create a new incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> create(@Valid @RequestBody IncidentRequest request) {
        IncidentResponse incident = incidentService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Incident created successfully", incident));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody IncidentRequest request) {
        IncidentResponse incident = incidentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incident updated successfully", incident));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an incident")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        incidentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Incident deleted successfully", null));
    }

    // Checklist Progress Endpoints
    @GetMapping("/{id}/checklists")
    @Operation(summary = "Get checklist progress for an incident")
    public ResponseEntity<ApiResponse<List<ChecklistProgressResponse>>> getChecklistProgress(@PathVariable UUID id) {
        List<ChecklistProgressResponse> progress = checklistService.getChecklistProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Checklist progress retrieved successfully", progress));
    }

    @PutMapping("/{id}/checklists/{checklistId}")
    @Operation(summary = "Update checklist status for an incident")
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
