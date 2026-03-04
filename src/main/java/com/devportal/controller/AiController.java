package com.devportal.controller;

import com.devportal.domain.enums.SummaryType;
import com.devportal.dto.response.*;
import com.devportal.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    // ==================== SMART SUMMARIES ====================

    @PostMapping("/summaries/generate")
    public ResponseEntity<ApiResponse<AiSummaryResponse>> generateSummary(
            @RequestParam String entityType,
            @RequestParam UUID entityId,
            @RequestParam SummaryType summaryType) {
        return ResponseEntity.ok(ApiResponse.success(
                aiService.generateSummary(entityType, entityId, summaryType)));
    }

    @GetMapping("/summaries")
    public ResponseEntity<ApiResponse<List<AiSummaryResponse>>> getSummaries(
            @RequestParam String entityType,
            @RequestParam UUID entityId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getSummaries(entityType, entityId)));
    }

    @PostMapping("/summaries/{summaryId}/approve")
    public ResponseEntity<ApiResponse<AiSummaryResponse>> approveSummary(@PathVariable UUID summaryId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.approveSummary(summaryId)));
    }

    // ==================== DUPLICATE DETECTION ====================

    @GetMapping("/duplicates/check")
    public ResponseEntity<ApiResponse<List<SimilaritySuggestionResponse>>> findSimilarItems(
            @RequestParam String entityType,
            @RequestParam String title,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(ApiResponse.success(
                aiService.findSimilarItems(entityType, title, description)));
    }

    @PostMapping("/duplicates/{suggestionId}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissSuggestion(@PathVariable UUID suggestionId) {
        aiService.dismissSuggestion(suggestionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== RECOMMENDATION ENGINE ====================

    @GetMapping("/recommendations/release/{releaseId}")
    public ResponseEntity<ApiResponse<List<ReleaseRecommendationResponse>>> getRecommendationsForRelease(
            @PathVariable UUID releaseId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getRecommendationsForRelease(releaseId)));
    }

    @PostMapping("/recommendations/{recommendationId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRecommendation(@PathVariable UUID recommendationId) {
        aiService.acceptRecommendation(recommendationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/recommendations/{recommendationId}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissRecommendation(@PathVariable UUID recommendationId) {
        aiService.dismissRecommendation(recommendationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
