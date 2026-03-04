package com.devportal.controller;

import com.devportal.domain.enums.TemplateEntityType;
import com.devportal.dto.request.TemplateRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.TemplateResponse;
import com.devportal.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success(templateService.getAllTemplates()));
    }

    @GetMapping("/type/{entityType}")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplatesByType(
            @PathVariable TemplateEntityType entityType) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getTemplatesByType(entityType)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getTemplateById(id)));
    }

    @GetMapping("/default/{entityType}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getDefaultTemplate(
            @PathVariable TemplateEntityType entityType) {
        TemplateResponse template = templateService.getDefaultTemplate(entityType);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(templateService.createTemplate(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(templateService.updateTemplate(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
