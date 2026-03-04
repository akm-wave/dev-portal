package com.devportal.controller;

import com.devportal.domain.enums.ReleaseStatus;
import com.devportal.dto.request.ReleaseLinkRequest;
import com.devportal.dto.request.ReleaseMicroserviceRequest;
import com.devportal.dto.request.ReleaseRequest;
import com.devportal.dto.response.*;
import com.devportal.service.ReleaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/releases")
@RequiredArgsConstructor
public class ReleaseController {

    private final ReleaseService releaseService;

    @GetMapping
    public ResponseEntity<PagedResponse<ReleaseResponse>> getAllReleases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) ReleaseStatus status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(releaseService.getAllReleases(page, size, sortBy, sortDir, status, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReleaseResponse> getReleaseById(@PathVariable UUID id) {
        return ResponseEntity.ok(releaseService.getReleaseById(id));
    }

    @PostMapping
    public ResponseEntity<ReleaseResponse> createRelease(@Valid @RequestBody ReleaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(releaseService.createRelease(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReleaseResponse> updateRelease(@PathVariable UUID id,
                                                          @Valid @RequestBody ReleaseRequest request) {
        return ResponseEntity.ok(releaseService.updateRelease(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelease(@PathVariable UUID id) {
        releaseService.deleteRelease(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/microservices")
    public ResponseEntity<List<ReleaseMicroserviceResponse>> getMicroservices(@PathVariable UUID id) {
        return ResponseEntity.ok(releaseService.getMicroservices(id));
    }

    @PostMapping("/{id}/microservices")
    public ResponseEntity<ReleaseMicroserviceResponse> addMicroservice(
            @PathVariable UUID id,
            @Valid @RequestBody ReleaseMicroserviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(releaseService.addMicroservice(id, request));
    }

    @PutMapping("/{id}/microservices/{microserviceId}")
    public ResponseEntity<ReleaseMicroserviceResponse> updateMicroservice(
            @PathVariable UUID id,
            @PathVariable UUID microserviceId,
            @Valid @RequestBody ReleaseMicroserviceRequest request) {
        return ResponseEntity.ok(releaseService.updateMicroservice(id, microserviceId, request));
    }

    @DeleteMapping("/{id}/microservices/{microserviceId}")
    public ResponseEntity<Void> removeMicroservice(@PathVariable UUID id, @PathVariable UUID microserviceId) {
        releaseService.removeMicroservice(id, microserviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<ReleaseLinkResponse>> getLinks(@PathVariable UUID id) {
        return ResponseEntity.ok(releaseService.getLinks(id));
    }

    @PostMapping("/{id}/links")
    public ResponseEntity<ReleaseLinkResponse> addLink(
            @PathVariable UUID id,
            @Valid @RequestBody ReleaseLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(releaseService.addLink(id, request));
    }

    @DeleteMapping("/{id}/links/{linkId}")
    public ResponseEntity<Void> removeLink(@PathVariable UUID id, @PathVariable UUID linkId) {
        releaseService.removeLink(id, linkId);
        return ResponseEntity.noContent().build();
    }
}
