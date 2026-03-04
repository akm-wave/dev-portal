package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ReleaseLinkType;
import com.devportal.domain.enums.ReleaseStatus;
import com.devportal.dto.request.ReleaseLinkRequest;
import com.devportal.dto.request.ReleaseMicroserviceRequest;
import com.devportal.dto.request.ReleaseRequest;
import com.devportal.dto.response.*;
import com.devportal.exception.BadRequestException;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ReleaseMicroserviceRepository releaseMicroserviceRepository;
    private final ReleaseLinkRepository releaseLinkRepository;
    private final MicroserviceRepository microserviceRepository;
    private final UserRepository userRepository;
    private final FeatureRepository featureRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final IssueRepository issueRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PagedResponse<ReleaseResponse> getAllReleases(int page, int size, String sortBy, String sortDir,
                                                          ReleaseStatus status, String search) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Release> releasePage;

        if (search != null && !search.isEmpty() && status != null) {
            releasePage = releaseRepository.searchByNameOrVersionAndStatus(search, status, pageable);
        } else if (search != null && !search.isEmpty()) {
            releasePage = releaseRepository.searchByNameOrVersion(search, pageable);
        } else if (status != null) {
            releasePage = releaseRepository.findByStatus(status, pageable);
        } else {
            releasePage = releaseRepository.findAll(pageable);
        }

        Page<ReleaseResponse> responsePage = releasePage.map(this::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public ReleaseResponse getReleaseById(UUID id) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));
        return toDetailResponse(release);
    }

    @Transactional
    public ReleaseResponse createRelease(ReleaseRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Release release = Release.builder()
                .name(request.getName())
                .version(request.getVersion())
                .releaseDate(request.getReleaseDate())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : ReleaseStatus.DRAFT)
                .oldBuildNumber(request.getOldBuildNumber())
                .featureBranch(request.getFeatureBranch())
                .createdBy(user)
                .build();

        release = releaseRepository.save(release);

        // Add microservices
        if (request.getMicroservices() != null && !request.getMicroservices().isEmpty()) {
            for (ReleaseMicroserviceRequest msRequest : request.getMicroservices()) {
                addMicroserviceToRelease(release, msRequest);
            }
        }

        // Add links
        if (request.getLinks() != null && !request.getLinks().isEmpty()) {
            for (ReleaseLinkRequest linkRequest : request.getLinks()) {
                addLinkToRelease(release, linkRequest);
            }
        }

        log.info("Release created: {} v{}", release.getName(), release.getVersion());
        activityLogService.logActivity("CREATE", "RELEASE", release.getId(), 
                "Created release: " + release.getName() + " v" + release.getVersion());

        return toDetailResponse(release);
    }

    @Transactional
    public ReleaseResponse updateRelease(UUID id, ReleaseRequest request) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));

        release.setName(request.getName());
        release.setVersion(request.getVersion());
        release.setReleaseDate(request.getReleaseDate());
        release.setDescription(request.getDescription());
        release.setOldBuildNumber(request.getOldBuildNumber());
        release.setFeatureBranch(request.getFeatureBranch());
        
        if (request.getStatus() != null) {
            ReleaseStatus oldStatus = release.getStatus();
            release.setStatus(request.getStatus());
            if (oldStatus != request.getStatus()) {
                activityLogService.logActivity("STATUS_CHANGE", "RELEASE", release.getId(),
                        "Release status changed: " + oldStatus + " -> " + request.getStatus());
            }
        }

        // Update microservices - clear and re-add
        if (request.getMicroservices() != null) {
            release.getReleaseMicroservices().clear();
            for (ReleaseMicroserviceRequest msRequest : request.getMicroservices()) {
                addMicroserviceToRelease(release, msRequest);
            }
        }

        // Update links - clear and re-add
        if (request.getLinks() != null) {
            release.getReleaseLinks().clear();
            for (ReleaseLinkRequest linkRequest : request.getLinks()) {
                addLinkToRelease(release, linkRequest);
            }
        }

        release = releaseRepository.save(release);
        log.info("Release updated: {} v{}", release.getName(), release.getVersion());
        activityLogService.logActivity("UPDATE", "RELEASE", release.getId(),
                "Updated release: " + release.getName() + " v" + release.getVersion());

        return toDetailResponse(release);
    }

    @Transactional
    public void deleteRelease(UUID id) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));

        releaseRepository.delete(release);
        log.info("Release deleted: {} v{}", release.getName(), release.getVersion());
        activityLogService.logActivity("DELETE", "RELEASE", id,
                "Deleted release: " + release.getName() + " v" + release.getVersion());
    }

    @Transactional
    public ReleaseMicroserviceResponse addMicroservice(UUID releaseId, ReleaseMicroserviceRequest request) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", releaseId));

        if (releaseMicroserviceRepository.existsByReleaseIdAndMicroserviceId(releaseId, request.getMicroserviceId())) {
            throw new BadRequestException("Microservice already added to this release");
        }

        ReleaseMicroservice rm = addMicroserviceToRelease(release, request);
        activityLogService.logActivity("ADD_MICROSERVICE", "RELEASE", releaseId,
                "Added microservice to release: " + rm.getMicroservice().getName());

        return toMicroserviceResponse(rm);
    }

    @Transactional
    public ReleaseMicroserviceResponse updateMicroservice(UUID releaseId, UUID microserviceId, 
                                                           ReleaseMicroserviceRequest request) {
        ReleaseMicroservice rm = releaseMicroserviceRepository.findByReleaseIdAndMicroserviceId(releaseId, microserviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Release microservice not found"));

        rm.setBranchName(request.getBranchName());
        rm.setBuildNumber(request.getBuildNumber());
        rm.setReleaseDate(request.getReleaseDate());
        rm.setNotes(request.getNotes());

        rm = releaseMicroserviceRepository.save(rm);
        activityLogService.logActivity("UPDATE_MICROSERVICE", "RELEASE", releaseId,
                "Updated microservice in release: " + rm.getMicroservice().getName());

        return toMicroserviceResponse(rm);
    }

    @Transactional
    public void removeMicroservice(UUID releaseId, UUID microserviceId) {
        ReleaseMicroservice rm = releaseMicroserviceRepository.findByReleaseIdAndMicroserviceId(releaseId, microserviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Release microservice not found"));

        releaseMicroserviceRepository.delete(rm);
        activityLogService.logActivity("REMOVE_MICROSERVICE", "RELEASE", releaseId,
                "Removed microservice from release: " + rm.getMicroservice().getName());
    }

    @Transactional
    public ReleaseLinkResponse addLink(UUID releaseId, ReleaseLinkRequest request) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", releaseId));

        if (releaseLinkRepository.existsByReleaseIdAndEntityTypeAndEntityId(
                releaseId, request.getEntityType(), request.getEntityId())) {
            throw new BadRequestException("Link already exists for this release");
        }

        ReleaseLink link = addLinkToRelease(release, request);
        activityLogService.logActivity("ADD_LINK", "RELEASE", releaseId,
                "Added " + request.getEntityType() + " link to release");

        return toLinkResponse(link);
    }

    @Transactional
    public void removeLink(UUID releaseId, UUID linkId) {
        ReleaseLink link = releaseLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Release link not found"));

        if (!link.getRelease().getId().equals(releaseId)) {
            throw new BadRequestException("Link does not belong to this release");
        }

        releaseLinkRepository.delete(link);
        activityLogService.logActivity("REMOVE_LINK", "RELEASE", releaseId,
                "Removed " + link.getEntityType() + " link from release");
    }

    @Transactional(readOnly = true)
    public List<ReleaseMicroserviceResponse> getMicroservices(UUID releaseId) {
        return releaseMicroserviceRepository.findByReleaseId(releaseId).stream()
                .map(this::toMicroserviceResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReleaseLinkResponse> getLinks(UUID releaseId) {
        return releaseLinkRepository.findByReleaseId(releaseId).stream()
                .map(this::toLinkResponse)
                .collect(Collectors.toList());
    }

    private ReleaseMicroservice addMicroserviceToRelease(Release release, ReleaseMicroserviceRequest request) {
        Microservice microservice = microserviceRepository.findById(request.getMicroserviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Microservice", "id", request.getMicroserviceId()));

        ReleaseMicroservice rm = ReleaseMicroservice.builder()
                .release(release)
                .microservice(microservice)
                .branchName(request.getBranchName())
                .buildNumber(request.getBuildNumber())
                .releaseDate(request.getReleaseDate() != null ? request.getReleaseDate() : release.getReleaseDate())
                .notes(request.getNotes())
                .build();

        return releaseMicroserviceRepository.save(rm);
    }

    private ReleaseLink addLinkToRelease(Release release, ReleaseLinkRequest request) {
        // Validate entity exists
        validateEntityExists(request.getEntityType(), request.getEntityId());

        ReleaseLink link = ReleaseLink.builder()
                .release(release)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .build();

        return releaseLinkRepository.save(link);
    }

    private void validateEntityExists(ReleaseLinkType entityType, UUID entityId) {
        boolean exists = switch (entityType) {
            case FEATURE -> featureRepository.existsById(entityId);
            case INCIDENT -> incidentRepository.existsById(entityId);
            case HOTFIX -> hotfixRepository.existsById(entityId);
            case ISSUE -> issueRepository.existsById(entityId);
        };

        if (!exists) {
            throw new ResourceNotFoundException(entityType.name(), "id", entityId);
        }
    }

    private String getEntityName(ReleaseLinkType entityType, UUID entityId) {
        return switch (entityType) {
            case FEATURE -> featureRepository.findById(entityId).map(Feature::getName).orElse("Unknown");
            case INCIDENT -> incidentRepository.findById(entityId).map(Incident::getTitle).orElse("Unknown");
            case HOTFIX -> hotfixRepository.findById(entityId).map(Hotfix::getTitle).orElse("Unknown");
            case ISSUE -> issueRepository.findById(entityId).map(Issue::getTitle).orElse("Unknown");
        };
    }

    private ReleaseResponse toResponse(Release release) {
        return ReleaseResponse.builder()
                .id(release.getId())
                .name(release.getName())
                .version(release.getVersion())
                .releaseDate(release.getReleaseDate())
                .description(release.getDescription())
                .status(release.getStatus())
                .oldBuildNumber(release.getOldBuildNumber())
                .featureBranch(release.getFeatureBranch())
                .createdBy(release.getCreatedBy() != null ? UserSummary.builder()
                        .id(release.getCreatedBy().getId())
                        .username(release.getCreatedBy().getUsername())
                        .fullName(release.getCreatedBy().getUsername())
                        .build() : null)
                .createdAt(release.getCreatedAt())
                .updatedAt(release.getUpdatedAt())
                .build();
    }

    private ReleaseResponse toDetailResponse(Release release) {
        ReleaseResponse response = toResponse(release);
        response.setMicroservices(release.getReleaseMicroservices().stream()
                .map(this::toMicroserviceResponse)
                .collect(Collectors.toList()));
        response.setLinks(release.getReleaseLinks().stream()
                .map(this::toLinkResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private ReleaseMicroserviceResponse toMicroserviceResponse(ReleaseMicroservice rm) {
        return ReleaseMicroserviceResponse.builder()
                .id(rm.getId())
                .microserviceId(rm.getMicroservice().getId())
                .microserviceName(rm.getMicroservice().getName())
                .branchName(rm.getBranchName())
                .buildNumber(rm.getBuildNumber())
                .releaseDate(rm.getReleaseDate())
                .notes(rm.getNotes())
                .createdAt(rm.getCreatedAt())
                .updatedAt(rm.getUpdatedAt())
                .build();
    }

    private ReleaseLinkResponse toLinkResponse(ReleaseLink link) {
        return ReleaseLinkResponse.builder()
                .id(link.getId())
                .entityType(link.getEntityType())
                .entityId(link.getEntityId())
                .entityName(getEntityName(link.getEntityType(), link.getEntityId()))
                .createdAt(link.getCreatedAt())
                .build();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
