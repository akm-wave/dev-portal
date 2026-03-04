package com.devportal.controller;

import com.devportal.dto.request.UtilityCategoryRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.UtilityCategoryResponse;
import com.devportal.service.UtilityCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/utility-categories")
@RequiredArgsConstructor
public class UtilityCategoryController {

    private final UtilityCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilityCategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<UtilityCategoryResponse>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getRootCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilityCategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UtilityCategoryResponse>> createCategory(
            @Valid @RequestBody UtilityCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilityCategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UtilityCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
