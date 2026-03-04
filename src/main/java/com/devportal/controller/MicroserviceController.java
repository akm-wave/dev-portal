package com.devportal.controller;

import com.devportal.domain.enums.MicroserviceStatus;
import com.devportal.dto.request.MicroserviceRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.MicroserviceResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.service.MicroserviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/microservices")
@RequiredArgsConstructor
@Tag(name = "Microservice", description = "Microservice management APIs")
public class MicroserviceController {

    private final MicroserviceService microserviceService;

    @GetMapping
    @Operation(summary = "Get all microservices with pagination and filtering")
    public ResponseEntity<ApiResponse<PagedResponse<MicroserviceResponse>>> getAllMicroservices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) MicroserviceStatus status,
            @RequestParam(required = false) String search) {
        PagedResponse<MicroserviceResponse> response = microserviceService.getAllMicroservices(
                page, size, sortBy, sortDir, status, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get microservice by ID")
    public ResponseEntity<ApiResponse<MicroserviceResponse>> getMicroserviceById(@PathVariable UUID id) {
        MicroserviceResponse response = microserviceService.getMicroserviceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new microservice with selected checklists")
    public ResponseEntity<ApiResponse<MicroserviceResponse>> createMicroservice(
            @Valid @RequestBody MicroserviceRequest request) {
        MicroserviceResponse response = microserviceService.createMicroservice(request);
        return ResponseEntity.ok(ApiResponse.success("Microservice created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing microservice")
    public ResponseEntity<ApiResponse<MicroserviceResponse>> updateMicroservice(
            @PathVariable UUID id,
            @Valid @RequestBody MicroserviceRequest request) {
        MicroserviceResponse response = microserviceService.updateMicroservice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Microservice updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a microservice")
    public ResponseEntity<ApiResponse<Void>> deleteMicroservice(@PathVariable UUID id) {
        microserviceService.deleteMicroservice(id);
        return ResponseEntity.ok(ApiResponse.success("Microservice deleted successfully", null));
    }
}
