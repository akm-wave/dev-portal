package com.devportal.controller;

import com.devportal.domain.enums.FeatureStatus;
import com.devportal.dto.request.FeatureRequest;
import com.devportal.dto.response.*;
import com.devportal.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/features")
@RequiredArgsConstructor
@Tag(name = "Feature", description = "Feature management APIs")
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping
    @Operation(summary = "Get all features with pagination and filtering")
    public ResponseEntity<ApiResponse<PagedResponse<FeatureResponse>>> getAllFeatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) FeatureStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID assignedToId) {
        PagedResponse<FeatureResponse> response = featureService.getAllFeatures(
                page, size, sortBy, sortDir, status, search, assignedToId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID")
    public ResponseEntity<ApiResponse<FeatureResponse>> getFeatureById(@PathVariable UUID id) {
        FeatureResponse response = featureService.getFeatureById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new feature with selected microservices")
    public ResponseEntity<ApiResponse<FeatureResponse>> createFeature(
            @Valid @RequestBody FeatureRequest request) {
        FeatureResponse response = featureService.createFeature(request);
        return ResponseEntity.ok(ApiResponse.success("Feature created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing feature")
    public ResponseEntity<ApiResponse<FeatureResponse>> updateFeature(
            @PathVariable UUID id,
            @Valid @RequestBody FeatureRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(ApiResponse.success("Feature updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a feature")
    public ResponseEntity<ApiResponse<Void>> deleteFeature(@PathVariable UUID id) {
        featureService.deleteFeature(id);
        return ResponseEntity.ok(ApiResponse.success("Feature deleted successfully", null));
    }

    @GetMapping("/{id}/microservices")
    @Operation(summary = "Get impacted microservices for a feature")
    public ResponseEntity<ApiResponse<List<MicroserviceResponse>>> getImpactedMicroservices(@PathVariable UUID id) {
        List<MicroserviceResponse> response = featureService.getImpactedMicroservices(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/checklists")
    @Operation(summary = "Get aggregated checklists for a feature")
    public ResponseEntity<ApiResponse<List<ChecklistResponse>>> getAggregatedChecklists(@PathVariable UUID id) {
        List<ChecklistResponse> response = featureService.getAggregatedChecklists(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
