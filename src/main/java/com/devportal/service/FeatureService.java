package com.devportal.service;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.entity.Feature;
import com.devportal.domain.entity.Microservice;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.domain.enums.FeatureStatus;
import com.devportal.domain.enums.MicroserviceStatus;
import com.devportal.dto.request.FeatureRequest;
import com.devportal.dto.response.ChecklistResponse;
import com.devportal.dto.response.FeatureResponse;
import com.devportal.dto.response.MicroserviceResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.exception.BadRequestException;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.mapper.ChecklistMapper;
import com.devportal.mapper.FeatureMapper;
import com.devportal.mapper.MicroserviceMapper;
import com.devportal.repository.FeatureRepository;
import com.devportal.repository.MicroserviceRepository;
import com.devportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final MicroserviceRepository microserviceRepository;
    private final UserRepository userRepository;
    private final FeatureMapper featureMapper;
    private final MicroserviceMapper microserviceMapper;
    private final ChecklistMapper checklistMapper;
    private final FeatureCheckpointService featureCheckpointService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PagedResponse<FeatureResponse> getAllFeatures(int page, int size, String sortBy, String sortDir,
                                                          FeatureStatus status, String search, UUID assignedToId) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Feature> featurePage;
        
        // If ownerId is provided, filter by owner (for non-admin users)
        if (assignedToId != null) {
            if (search != null && !search.isEmpty()) {
                featurePage = featureRepository.searchByOwnerIdAndNameOrDescription(assignedToId, search, pageable);
            } else if (status != null) {
                featurePage = featureRepository.findByOwnerIdAndStatus(assignedToId, status, pageable);
            } else {
                featurePage = featureRepository.findByOwnerId(assignedToId, pageable);
            }
        } else {
            if (search != null && !search.isEmpty()) {
                featurePage = featureRepository.searchByNameOrDescription(search, pageable);
            } else if (status != null) {
                featurePage = featureRepository.findByStatus(status, pageable);
            } else {
                featurePage = featureRepository.findAll(pageable);
            }
        }

        Page<FeatureResponse> responsePage = featurePage.map(featureMapper::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public FeatureResponse getFeatureById(UUID id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", id));
        return featureMapper.toResponse(feature);
    }

    @Transactional
    public FeatureResponse createFeature(FeatureRequest request) {
        if (request.getMicroserviceIds() == null || request.getMicroserviceIds().isEmpty()) {
            throw new BadRequestException("At least one microservice is required");
        }

        List<Microservice> microservices = microserviceRepository.findByIdIn(request.getMicroserviceIds());
        if (microservices.size() != request.getMicroserviceIds().size()) {
            throw new BadRequestException("One or more microservices not found");
        }

        Feature feature = featureMapper.toEntity(request);
        feature.setMicroservices(new HashSet<>(microservices));
        
        if (feature.getStatus() == null) {
            feature.setStatus(FeatureStatus.PLANNED);
        }
        
        // Set owner if provided
        if (request.getOwnerId() != null) {
            feature.setOwner(userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new BadRequestException("Owner not found")));
        }

        feature = featureRepository.save(feature);
        
        // Auto-generate checkpoints from linked microservices
        featureCheckpointService.generateCheckpointsForFeature(feature, feature.getMicroservices());
        
        log.info("Feature created: {}", feature.getName());
        activityLogService.logActivity("CREATE", "FEATURE", feature.getId(), "Created feature: " + feature.getName());
        return featureMapper.toResponse(feature);
    }

    @Transactional
    public FeatureResponse updateFeature(UUID id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", id));

        if (request.getMicroserviceIds() == null || request.getMicroserviceIds().isEmpty()) {
            throw new BadRequestException("At least one microservice is required");
        }

        List<Microservice> microservices = microserviceRepository.findByIdIn(request.getMicroserviceIds());
        if (microservices.size() != request.getMicroserviceIds().size()) {
            throw new BadRequestException("One or more microservices not found");
        }

        featureMapper.updateEntity(request, feature);
        feature.setMicroservices(new HashSet<>(microservices));
        
        // Update owner
        if (request.getOwnerId() != null) {
            feature.setOwner(userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new BadRequestException("Owner not found")));
        } else {
            feature.setOwner(null);
        }
        
        // Auto-update status based on microservice completion
        updateFeatureStatus(feature);

        feature = featureRepository.save(feature);
        
        // Sync checkpoints when microservices are updated (add new ones, keep existing)
        featureCheckpointService.generateCheckpointsForFeature(feature, feature.getMicroservices());
        
        log.info("Feature updated: {}", feature.getName());
        activityLogService.logActivity("UPDATE", "FEATURE", feature.getId(), "Updated feature: " + feature.getName());
        return featureMapper.toResponse(feature);
    }

    @Transactional
    public void deleteFeature(UUID id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", id));

        featureRepository.delete(feature);
        log.info("Feature deleted: {}", feature.getName());
        activityLogService.logActivity("DELETE", "FEATURE", id, "Deleted feature: " + feature.getName());
    }

    @Transactional(readOnly = true)
    public List<MicroserviceResponse> getImpactedMicroservices(UUID featureId) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", featureId));
        
        return feature.getMicroservices().stream()
                .map(microserviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChecklistResponse> getAggregatedChecklists(UUID featureId) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", "id", featureId));
        
        Set<Checklist> allChecklists = feature.getMicroservices().stream()
                .flatMap(m -> m.getChecklists().stream())
                .collect(Collectors.toSet());
        
        return allChecklists.stream()
                .map(checklistMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void updateFeatureStatus(Feature feature) {
        Set<Microservice> microservices = feature.getMicroservices();
        if (microservices == null || microservices.isEmpty()) return;

        long completedCount = microservices.stream()
                .filter(m -> m.getStatus() == MicroserviceStatus.COMPLETED)
                .count();

        if (completedCount == microservices.size()) {
            feature.setStatus(FeatureStatus.RELEASED);
        } else if (completedCount > 0 || microservices.stream().anyMatch(m -> m.getStatus() == MicroserviceStatus.IN_PROGRESS)) {
            feature.setStatus(FeatureStatus.IN_PROGRESS);
        }
    }
}
