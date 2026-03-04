package com.devportal.service;

import com.devportal.domain.entity.Domain;
import com.devportal.dto.request.DomainRequest;
import com.devportal.dto.response.DomainResponse;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainService {

    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public List<DomainResponse> getAllDomains() {
        return domainRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DomainResponse> getAllDomainsIncludingInactive() {
        return domainRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DomainResponse getDomainById(UUID id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + id));
        return toResponse(domain);
    }

    @Transactional
    public DomainResponse createDomain(DomainRequest request) {
        if (domainRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Domain with name '" + request.getName() + "' already exists");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Domain domain = Domain.builder()
                .name(request.getName())
                .description(request.getDescription())
                .colorCode(request.getColorCode())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(username)
                .build();

        domain = domainRepository.save(domain);
        log.info("Domain created: {} by {}", domain.getName(), username);
        return toResponse(domain);
    }

    @Transactional
    public DomainResponse updateDomain(UUID id, DomainRequest request) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + id));

        if (domainRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Domain with name '" + request.getName() + "' already exists");
        }

        domain.setName(request.getName());
        domain.setDescription(request.getDescription());
        domain.setColorCode(request.getColorCode());
        if (request.getIsActive() != null) {
            domain.setIsActive(request.getIsActive());
        }

        domain = domainRepository.save(domain);
        log.info("Domain updated: {}", domain.getName());
        return toResponse(domain);
    }

    @Transactional
    public void deleteDomain(UUID id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + id));
        
        domainRepository.delete(domain);
        log.info("Domain deleted: {}", domain.getName());
    }

    private DomainResponse toResponse(Domain domain) {
        return DomainResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .colorCode(domain.getColorCode())
                .isActive(domain.getIsActive())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
