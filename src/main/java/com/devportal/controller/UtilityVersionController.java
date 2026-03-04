package com.devportal.controller;

import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.UtilityVersionResponse;
import com.devportal.service.UtilityVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/utilities/{utilityId}/versions")
@RequiredArgsConstructor
public class UtilityVersionController {

    private final UtilityVersionService versionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilityVersionResponse>>> getVersionHistory(
            @PathVariable UUID utilityId) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getVersionHistory(utilityId)));
    }

    @GetMapping("/{versionNumber}")
    public ResponseEntity<ApiResponse<UtilityVersionResponse>> getVersion(
            @PathVariable UUID utilityId,
            @PathVariable Integer versionNumber) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getVersion(utilityId, versionNumber)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UtilityVersionResponse>> getCurrentVersion(
            @PathVariable UUID utilityId) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getCurrentVersion(utilityId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UtilityVersionResponse>> createVersion(
            @PathVariable UUID utilityId,
            @RequestParam(required = false) String changeSummary) {
        return ResponseEntity.ok(ApiResponse.success(versionService.createVersion(utilityId, changeSummary)));
    }

    @PostMapping("/revert/{versionNumber}")
    public ResponseEntity<ApiResponse<UtilityVersionResponse>> revertToVersion(
            @PathVariable UUID utilityId,
            @PathVariable Integer versionNumber) {
        return ResponseEntity.ok(ApiResponse.success(versionService.revertToVersion(utilityId, versionNumber)));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<String>> compareVersions(
            @PathVariable UUID utilityId,
            @RequestParam Integer version1,
            @RequestParam Integer version2) {
        return ResponseEntity.ok(ApiResponse.success(versionService.compareVersions(utilityId, version1, version2)));
    }
}
