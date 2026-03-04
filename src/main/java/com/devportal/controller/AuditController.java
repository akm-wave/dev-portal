package com.devportal.controller;

import com.devportal.dto.response.ActivityLogResponse;
import com.devportal.dto.response.ApiResponse;
import com.devportal.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log APIs")
public class AuditController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "Get all audit logs with pagination and filters")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        Page<ActivityLogResponse> logs = activityLogService.searchActivities(entityType, action, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }

    @GetMapping("/entity-types")
    @Operation(summary = "Get distinct entity types")
    public ResponseEntity<ApiResponse<String[]>> getEntityTypes() {
        String[] entityTypes = {"FEATURE", "MICROSERVICE", "CHECKLIST", "INCIDENT", "HOTFIX", "ISSUE", "USER", "DOMAIN"};
        return ResponseEntity.ok(ApiResponse.success("Entity types retrieved successfully", entityTypes));
    }

    @GetMapping("/actions")
    @Operation(summary = "Get distinct actions")
    public ResponseEntity<ApiResponse<String[]>> getActions() {
        String[] actions = {"CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "STATUS_CHANGE", "UPLOAD", "DOWNLOAD"};
        return ResponseEntity.ok(ApiResponse.success("Actions retrieved successfully", actions));
    }
}
