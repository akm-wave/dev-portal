package com.devportal.controller;

import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.WorkspaceProductivityDTO;
import com.devportal.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/workspace")
@RequiredArgsConstructor
@Tag(name = "Workspace", description = "Workspace management APIs")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/productivity")
    @Operation(summary = "Get user productivity dashboard", description = "Returns comprehensive productivity metrics for the logged-in user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkspaceProductivityDTO>> getMyProductivityDashboard(
            @Parameter(description = "Date range filter: today, this_week, this_month, or custom")
            @RequestParam(defaultValue = "this_month") String dateRange) {
        
        log.info("Fetching productivity dashboard for date range: {}", dateRange);
        
        WorkspaceProductivityDTO dashboard = workspaceService.getMyProductivityDashboard(dateRange);
        
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/productivity/{userId}")
    @Operation(summary = "Get user productivity dashboard (Admin only)", description = "Returns productivity metrics for a specific user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkspaceProductivityDTO>> getUserProductivityDashboard(
            @Parameter(description = "User ID")
            @PathVariable UUID userId,
            @Parameter(description = "Date range filter: today, this_week, this_month, or custom")
            @RequestParam(defaultValue = "this_month") String dateRange) {
        
        log.info("Fetching productivity dashboard for user {} with date range: {}", userId, dateRange);
        
        // Note: This would require modifying the service to accept userId parameter
        // For now, we'll return unauthorized
        return ResponseEntity.status(403).body(ApiResponse.error("Admin access to user productivity not yet implemented"));
    }
}
