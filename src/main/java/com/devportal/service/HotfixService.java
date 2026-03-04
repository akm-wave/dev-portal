package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.HotfixStatus;
import com.devportal.dto.request.HotfixRequest;
import com.devportal.dto.response.HotfixResponse;
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
public class HotfixService {

    private final HotfixRepository hotfixRepository;
    private final FeatureRepository featureRepository;
    private final MicroserviceRepository microserviceRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public Page<HotfixResponse> getAll(Pageable pageable) {
        return hotfixRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public HotfixResponse getById(UUID id) {
        Hotfix hotfix = hotfixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotfix not found: " + id));
        return toResponse(hotfix);
    }

    @Transactional(readOnly = true)
    public List<HotfixResponse> getByFeatureId(UUID featureId) {
        return hotfixRepository.findByMainFeatureId(featureId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotfixResponse create(HotfixRequest request) {
        Feature feature = featureRepository.findById(request.getMainFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + request.getMainFeatureId()));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Hotfix hotfix = Hotfix.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .releaseVersion(request.getReleaseVersion())
                .status(request.getStatus() != null ? request.getStatus() : HotfixStatus.PLANNED)
                .mainFeature(feature)
                .createdBy(username)
                .build();

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getOwnerId()));
            hotfix.setOwner(owner);
        }

        if (request.getMicroserviceIds() != null && !request.getMicroserviceIds().isEmpty()) {
            Set<Microservice> microservices = new HashSet<>(microserviceRepository.findAllById(request.getMicroserviceIds()));
            hotfix.setMicroservices(microservices);
        }

        hotfix = hotfixRepository.save(hotfix);
        activityLogService.logActivity("CREATE", "HOTFIX", hotfix.getId(), "Created hotfix: " + hotfix.getTitle());
        return toResponse(hotfix);
    }

    @Transactional
    public HotfixResponse update(UUID id, HotfixRequest request) {
        Hotfix hotfix = hotfixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotfix not found: " + id));

        hotfix.setTitle(request.getTitle());
        hotfix.setDescription(request.getDescription());
        hotfix.setReleaseVersion(request.getReleaseVersion());

        if (request.getStatus() != null) {
            HotfixStatus oldStatus = hotfix.getStatus();
            hotfix.setStatus(request.getStatus());
            if (request.getStatus() == HotfixStatus.DEPLOYED && oldStatus != HotfixStatus.DEPLOYED) {
                hotfix.setDeployedAt(LocalDateTime.now());
            }
        }

        if (request.getMainFeatureId() != null) {
            Feature feature = featureRepository.findById(request.getMainFeatureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + request.getMainFeatureId()));
            hotfix.setMainFeature(feature);
        }

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getOwnerId()));
            hotfix.setOwner(owner);
        }

        if (request.getMicroserviceIds() != null) {
            Set<Microservice> microservices = new HashSet<>(microserviceRepository.findAllById(request.getMicroserviceIds()));
            hotfix.setMicroservices(microservices);
        }

        hotfix = hotfixRepository.save(hotfix);
        activityLogService.logActivity("UPDATE", "HOTFIX", hotfix.getId(), "Updated hotfix: " + hotfix.getTitle());
        return toResponse(hotfix);
    }

    @Transactional
    public void delete(UUID id) {
        Hotfix hotfix = hotfixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotfix not found: " + id));
        hotfixRepository.deleteById(id);
        activityLogService.logActivity("DELETE", "HOTFIX", id, "Deleted hotfix: " + hotfix.getTitle());
    }

    private HotfixResponse toResponse(Hotfix hotfix) {
        List<MicroserviceSummary> msSummaries = hotfix.getMicroservices().stream()
                .map(ms -> MicroserviceSummary.builder()
                        .id(ms.getId())
                        .name(ms.getName())
                        .status(ms.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return HotfixResponse.builder()
                .id(hotfix.getId())
                .title(hotfix.getTitle())
                .description(hotfix.getDescription())
                .releaseVersion(hotfix.getReleaseVersion())
                .status(hotfix.getStatus())
                .mainFeature(FeatureSummary.builder()
                        .id(hotfix.getMainFeature().getId())
                        .name(hotfix.getMainFeature().getName())
                        .domain(hotfix.getMainFeature().getDomain())
                        .build())
                .owner(hotfix.getOwner() != null ? UserSummary.builder()
                        .id(hotfix.getOwner().getId())
                        .username(hotfix.getOwner().getUsername())
                        .fullName(hotfix.getOwner().getUsername())
                        .build() : null)
                .createdBy(hotfix.getCreatedBy())
                .deployedAt(hotfix.getDeployedAt())
                .createdAt(hotfix.getCreatedAt())
                .updatedAt(hotfix.getUpdatedAt())
                .microservices(msSummaries)
                .microserviceCount(msSummaries.size())
                .build();
    }
}
