package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.IncidentStatus;
import com.devportal.dto.request.IncidentRequest;
import com.devportal.dto.response.IncidentResponse;
import com.devportal.dto.response.IncidentResponse.*;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final FeatureRepository featureRepository;
    private final MicroserviceRepository microserviceRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getAll(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        return toResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getByFeatureId(UUID featureId) {
        return incidentRepository.findByMainFeatureId(featureId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncidentResponse create(IncidentRequest request) {
        Feature feature = featureRepository.findById(request.getMainFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + request.getMainFeatureId()));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(request.getStatus() != null ? request.getStatus() : IncidentStatus.OPEN)
                .mainFeature(feature)
                .createdBy(username)
                .build();

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getOwnerId()));
            incident.setOwner(owner);
        }

        if (request.getMicroserviceIds() != null && !request.getMicroserviceIds().isEmpty()) {
            Set<Microservice> microservices = new HashSet<>(microserviceRepository.findAllById(request.getMicroserviceIds()));
            incident.setMicroservices(microservices);
        }

        incident = incidentRepository.save(incident);
        activityLogService.logActivity("CREATE", "INCIDENT", incident.getId(), "Created incident: " + incident.getTitle());
        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse update(UUID id, IncidentRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));

        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());

        if (request.getStatus() != null) {
            IncidentStatus oldStatus = incident.getStatus();
            incident.setStatus(request.getStatus());
            if (request.getStatus() == IncidentStatus.RESOLVED && oldStatus != IncidentStatus.RESOLVED) {
                incident.setResolvedAt(LocalDateTime.now());
            }
        }

        if (request.getMainFeatureId() != null) {
            Feature feature = featureRepository.findById(request.getMainFeatureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + request.getMainFeatureId()));
            incident.setMainFeature(feature);
        }

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getOwnerId()));
            incident.setOwner(owner);
        }

        if (request.getMicroserviceIds() != null) {
            Set<Microservice> microservices = new HashSet<>(microserviceRepository.findAllById(request.getMicroserviceIds()));
            incident.setMicroservices(microservices);
        }

        incident = incidentRepository.save(incident);
        activityLogService.logActivity("UPDATE", "INCIDENT", incident.getId(), "Updated incident: " + incident.getTitle());
        return toResponse(incident);
    }

    @Transactional
    public void delete(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        incidentRepository.deleteById(id);
        activityLogService.logActivity("DELETE", "INCIDENT", id, "Deleted incident: " + incident.getTitle());
    }

    private IncidentResponse toResponse(Incident incident) {
        List<MicroserviceSummary> msSummaries = incident.getMicroservices().stream()
                .map(ms -> MicroserviceSummary.builder()
                        .id(ms.getId())
                        .name(ms.getName())
                        .status(ms.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .mainFeature(FeatureSummary.builder()
                        .id(incident.getMainFeature().getId())
                        .name(incident.getMainFeature().getName())
                        .domain(incident.getMainFeature().getDomain())
                        .build())
                .owner(incident.getOwner() != null ? UserSummary.builder()
                        .id(incident.getOwner().getId())
                        .username(incident.getOwner().getUsername())
                        .fullName(incident.getOwner().getUsername())
                        .build() : null)
                .createdBy(incident.getCreatedBy())
                .resolvedAt(incident.getResolvedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .microservices(msSummaries)
                .microserviceCount(msSummaries.size())
                .build();
    }
}
