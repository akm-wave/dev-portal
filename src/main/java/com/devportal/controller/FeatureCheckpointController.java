package com.devportal.controller;

import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.FeatureCheckpointResponse;
import com.devportal.dto.response.FileDownloadResponse;
import com.devportal.service.FeatureCheckpointService;
import com.devportal.service.GridFsFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/features/{featureId}/checkpoints")
@RequiredArgsConstructor
@Tag(name = "Feature Checkpoints", description = "Feature-specific checkpoint execution APIs")
public class FeatureCheckpointController {

    private final FeatureCheckpointService featureCheckpointService;
    private final GridFsFileService gridFsFileService;

    @GetMapping
    @Operation(summary = "Get all checkpoints for a feature")
    public ResponseEntity<ApiResponse<List<FeatureCheckpointResponse>>> getCheckpoints(@PathVariable UUID featureId) {
        List<FeatureCheckpointResponse> checkpoints = featureCheckpointService.getCheckpointsByFeatureId(featureId);
        return ResponseEntity.ok(ApiResponse.success("Checkpoints retrieved successfully", checkpoints));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get checkpoint statistics for a feature")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCheckpointStats(@PathVariable UUID featureId) {
        Map<String, Long> stats = featureCheckpointService.getCheckpointStatsByFeatureId(featureId);
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", stats));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get feature progress percentage")
    public ResponseEntity<ApiResponse<Double>> getProgress(@PathVariable UUID featureId) {
        double progress = featureCheckpointService.calculateFeatureProgress(featureId);
        return ResponseEntity.ok(ApiResponse.success("Progress calculated", progress));
    }

    @PostMapping("/{checkpointId}/attachment")
    @Operation(summary = "Upload attachment for a checkpoint")
    public ResponseEntity<ApiResponse<FeatureCheckpointResponse>> uploadAttachment(
            @PathVariable UUID featureId,
            @PathVariable UUID checkpointId,
            @RequestParam("file") MultipartFile file) throws IOException {
        FeatureCheckpointResponse checkpoint = featureCheckpointService.uploadAttachment(featureId, checkpointId, file);
        return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully", checkpoint));
    }

    @GetMapping("/{checkpointId}/attachment")
    @Operation(summary = "Download attachment for a checkpoint")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable UUID featureId,
            @PathVariable UUID checkpointId) {
        FeatureCheckpointResponse checkpoint = featureCheckpointService.getCheckpoint(featureId, checkpointId);
        if (checkpoint.getMongoFileId() == null) {
            throw new RuntimeException("No attachment found");
        }
        FileDownloadResponse file = gridFsFileService.downloadFile(checkpoint.getMongoFileId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getData());
    }

    @DeleteMapping("/{checkpointId}/attachment")
    @Operation(summary = "Delete attachment for a checkpoint")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable UUID featureId,
            @PathVariable UUID checkpointId) {
        featureCheckpointService.deleteAttachment(featureId, checkpointId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
