package com.devportal.controller;

import com.devportal.domain.enums.ChangeType;
import com.devportal.dto.request.ImpactCalculationRequest;
import com.devportal.dto.response.ImpactAnalysisResponse;
import com.devportal.service.ImpactCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/impact")
@RequiredArgsConstructor
public class ImpactController {

    private final ImpactCalculatorService impactCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<ImpactAnalysisResponse> calculateImpact(@RequestBody ImpactCalculationRequest request) {
        ImpactAnalysisResponse response = impactCalculatorService.calculateImpact(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/preview")
    public ResponseEntity<ImpactAnalysisResponse> previewImpact(@RequestBody ImpactCalculationRequest request) {
        ImpactAnalysisResponse response = impactCalculatorService.calculateImpactPreview(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feature/{featureId}")
    public ResponseEntity<ImpactAnalysisResponse> getFeatureImpact(@PathVariable UUID featureId) {
        ImpactAnalysisResponse response = impactCalculatorService.getLatestAnalysis(featureId, null, null, null);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feature/{featureId}/history")
    public ResponseEntity<List<ImpactAnalysisResponse>> getFeatureImpactHistory(@PathVariable UUID featureId) {
        List<ImpactAnalysisResponse> history = impactCalculatorService.getAnalysisHistory(featureId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<ImpactAnalysisResponse> getIncidentImpact(@PathVariable UUID incidentId) {
        ImpactAnalysisResponse response = impactCalculatorService.getLatestAnalysis(null, incidentId, null, null);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hotfix/{hotfixId}")
    public ResponseEntity<ImpactAnalysisResponse> getHotfixImpact(@PathVariable UUID hotfixId) {
        ImpactAnalysisResponse response = impactCalculatorService.getLatestAnalysis(null, null, hotfixId, null);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/issue/{issueId}")
    public ResponseEntity<ImpactAnalysisResponse> getIssueImpact(@PathVariable UUID issueId) {
        ImpactAnalysisResponse response = impactCalculatorService.getLatestAnalysis(null, null, null, issueId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/change-types")
    public ResponseEntity<List<Map<String, Object>>> getChangeTypes() {
        List<Map<String, Object>> changeTypes = Arrays.stream(ChangeType.values())
                .map(ct -> Map.<String, Object>of(
                        "value", ct.name(),
                        "label", ct.getDisplayName(),
                        "riskWeight", ct.getRiskWeight()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(changeTypes);
    }
}
