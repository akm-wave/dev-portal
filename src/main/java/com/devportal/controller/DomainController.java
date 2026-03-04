package com.devportal.controller;

import com.devportal.dto.request.DomainRequest;
import com.devportal.dto.response.DomainResponse;
import com.devportal.service.DomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;

    @GetMapping
    public ResponseEntity<List<DomainResponse>> getAllDomains() {
        return ResponseEntity.ok(domainService.getAllDomains());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DomainResponse>> getAllDomainsIncludingInactive() {
        return ResponseEntity.ok(domainService.getAllDomainsIncludingInactive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainResponse> getDomainById(@PathVariable UUID id) {
        return ResponseEntity.ok(domainService.getDomainById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> createDomain(@Valid @RequestBody DomainRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(domainService.createDomain(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> updateDomain(@PathVariable UUID id, @Valid @RequestBody DomainRequest request) {
        return ResponseEntity.ok(domainService.updateDomain(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDomain(@PathVariable UUID id) {
        domainService.deleteDomain(id);
        return ResponseEntity.noContent().build();
    }
}
