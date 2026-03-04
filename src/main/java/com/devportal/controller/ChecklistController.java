package com.devportal.controller;

import com.devportal.domain.enums.ChecklistPriority;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.request.ChecklistRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.ChecklistResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklist", description = "Checklist management APIs")
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping
    @Operation(summary = "Get all checklists with pagination and filtering")
    public ResponseEntity<ApiResponse<PagedResponse<ChecklistResponse>>> getAllChecklists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) ChecklistStatus status,
            @RequestParam(required = false) ChecklistPriority priority,
            @RequestParam(required = false) String search) {
        PagedResponse<ChecklistResponse> response = checklistService.getAllChecklists(
                page, size, sortBy, sortDir, status, priority, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checklist by ID")
    public ResponseEntity<ApiResponse<ChecklistResponse>> getChecklistById(@PathVariable UUID id) {
        ChecklistResponse response = checklistService.getChecklistById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new checklist")
    public ResponseEntity<ApiResponse<ChecklistResponse>> createChecklist(
            @Valid @RequestBody ChecklistRequest request) {
        ChecklistResponse response = checklistService.createChecklist(request);
        return ResponseEntity.ok(ApiResponse.success("Checklist created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing checklist")
    public ResponseEntity<ApiResponse<ChecklistResponse>> updateChecklist(
            @PathVariable UUID id,
            @Valid @RequestBody ChecklistRequest request) {
        ChecklistResponse response = checklistService.updateChecklist(id, request);
        return ResponseEntity.ok(ApiResponse.success("Checklist updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a checklist (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteChecklist(@PathVariable UUID id) {
        checklistService.deleteChecklist(id);
        return ResponseEntity.ok(ApiResponse.success("Checklist deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update checklist status")
    public ResponseEntity<ApiResponse<ChecklistResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam ChecklistStatus status) {
        ChecklistResponse response = checklistService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }
}
