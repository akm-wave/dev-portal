package com.devportal.controller;

import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.GlobalSearchResult;
import com.devportal.service.GlobalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/global-search")
@RequiredArgsConstructor
@Tag(name = "Global Search", description = "Cross-module search APIs")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    @Operation(summary = "Search across all modules")
    public ResponseEntity<ApiResponse<GlobalSearchResult>> search(
            @RequestParam(name = "q", required = false, defaultValue = "") String query,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        GlobalSearchResult result = globalSearchService.search(query, limit);
        return ResponseEntity.ok(ApiResponse.success("Search completed", result));
    }
}
