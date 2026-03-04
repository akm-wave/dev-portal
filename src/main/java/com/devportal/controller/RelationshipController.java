package com.devportal.controller;

import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.RelationshipResponse;
import com.devportal.service.RelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relationships")
@RequiredArgsConstructor
@Tag(name = "Relationships", description = "Feature-Microservice relationship visualization APIs")
public class RelationshipController {

    private final RelationshipService relationshipService;

    @GetMapping
    @Operation(summary = "Get all relationships between features and microservices")
    public ResponseEntity<ApiResponse<RelationshipResponse>> getRelationships() {
        RelationshipResponse response = relationshipService.getRelationships();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
