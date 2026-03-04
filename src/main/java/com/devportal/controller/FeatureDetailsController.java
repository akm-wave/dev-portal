package com.devportal.controller;

import com.devportal.dto.request.CheckpointProgressRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.FeatureDetailsResponse;
import com.devportal.dto.response.FeatureDetailsResponse.CheckpointAnalysis;
import com.devportal.service.FeatureDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/features")
@RequiredArgsConstructor
@Tag(name = "Feature Details", description = "Feature details and checkpoint management APIs")
public class FeatureDetailsController {

    private final FeatureDetailsService featureDetailsService;

    @GetMapping("/{id}/details")
    @Operation(summary = "Get feature details with unique microservices and checkpoints")
    public ResponseEntity<ApiResponse<FeatureDetailsResponse>> getFeatureDetails(@PathVariable UUID id) {
        FeatureDetailsResponse details = featureDetailsService.getFeatureDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Feature details retrieved successfully", details));
    }

    @GetMapping("/{id}/checkpoints/unique")
    @Operation(summary = "Get unique checkpoints for a feature (deduplicated)")
    public ResponseEntity<ApiResponse<List<CheckpointAnalysis>>> getUniqueCheckpoints(@PathVariable UUID id) {
        List<CheckpointAnalysis> checkpoints = featureDetailsService.getUniqueCheckpoints(id);
        return ResponseEntity.ok(ApiResponse.success("Unique checkpoints retrieved successfully", checkpoints));
    }

    @PatchMapping("/{featureId}/checkpoints/{checklistId}")
    @Operation(summary = "Update checkpoint progress (status, remark, attachment) - checklistId is the checklist template ID")
    public ResponseEntity<ApiResponse<CheckpointAnalysis>> updateCheckpointProgress(
            @PathVariable UUID featureId,
            @PathVariable UUID checklistId,
            @RequestBody CheckpointProgressRequest request) {
        CheckpointAnalysis updated = featureDetailsService.updateCheckpointProgress(featureId, checklistId, request);
        return ResponseEntity.ok(ApiResponse.success("Checkpoint progress updated successfully", updated));
    }

    @PostMapping("/{id}/checkpoints/link")
    @Operation(summary = "Link additional checkpoints to a feature")
    public ResponseEntity<ApiResponse<Void>> linkCheckpoints(
            @PathVariable UUID id,
            @RequestBody List<UUID> checklistIds) {
        featureDetailsService.linkCheckpoints(id, checklistIds);
        return ResponseEntity.ok(ApiResponse.success("Checkpoints linked successfully", null));
    }
}
