package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.IssueStatus;
import com.devportal.dto.request.IssueRequest;
import com.devportal.dto.response.IssueResponse;
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
public class IssueService {

    private final IssueRepository issueRepository;
    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public Page<IssueResponse> getAll(Pageable pageable) {
        return issueRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public IssueResponse getById(UUID id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
        return toResponse(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getByFeatureId(UUID featureId) {
        return issueRepository.findByMainFeatureId(featureId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getByAssignedUser(UUID userId) {
        return issueRepository.findByAssignedToId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse create(IssueRequest request) {
        Feature feature = featureRepository.findById(request.getMainFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + request.getMainFeatureId()));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(IssueStatus.OPEN)
                .category(request.getCategory())
                .mainFeature(feature)
                .createdBy(username)
                .build();

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getOwnerId()));
            issue.setOwner(owner);
        }

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedToId()));
            issue.setAssignedTo(assignee);
            issue.setStatus(IssueStatus.ASSIGNED);
        }

        issue = issueRepository.save(issue);
        activityLogService.logActivity("CREATE", "ISSUE", issue.getId(), "Created issue: " + issue.getTitle());
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse update(UUID id, IssueRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            IssueStatus oldStatus = issue.getStatus();
            issue.setStatus(request.getStatus());
            if (request.getStatus() == IssueStatus.RESOLVED && oldStatus != IssueStatus.RESOLVED) {
                issue.setResolvedAt(LocalDateTime.now());
            }
        }

        if (request.getCategory() != null) {
            issue.setCategory(request.getCategory());
        }

        if (request.getMainFeatureId() != null) {
            Feature feature = featureRepository.findById(request.getMainFeatureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feature not found: " + request.getMainFeatureId()));
            issue.setMainFeature(feature);
        }

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getOwnerId()));
            issue.setOwner(owner);
        }

        if (request.getResultComment() != null) {
            issue.setResultComment(request.getResultComment());
        }

        if (request.getAttachmentUrl() != null) {
            issue.setAttachmentUrl(request.getAttachmentUrl());
        }

        issue = issueRepository.save(issue);
        activityLogService.logActivity("UPDATE", "ISSUE", issue.getId(), "Updated issue: " + issue.getTitle());
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse assignIssue(UUID id, UUID userId) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        issue.setAssignedTo(assignee);
        if (issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.ASSIGNED);
        }

        issue = issueRepository.save(issue);
        activityLogService.logActivity("ASSIGN", "ISSUE", issue.getId(), "Assigned issue: " + issue.getTitle());
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse resolveIssue(UUID id, String resultComment, String attachmentUrl) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));

        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolvedAt(LocalDateTime.now());
        if (resultComment != null) {
            issue.setResultComment(resultComment);
        }
        if (attachmentUrl != null) {
            issue.setAttachmentUrl(attachmentUrl);
        }

        issue = issueRepository.save(issue);
        activityLogService.logActivity("RESOLVE", "ISSUE", issue.getId(), "Resolved issue: " + issue.getTitle());
        return toResponse(issue);
    }

    @Transactional
    public void delete(UUID id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
        issueRepository.deleteById(id);
        activityLogService.logActivity("DELETE", "ISSUE", id, "Deleted issue: " + issue.getTitle());
    }

    private IssueResponse toResponse(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .priority(issue.getPriority())
                .status(issue.getStatus())
                .category(issue.getCategory())
                .mainFeature(FeatureSummary.builder()
                        .id(issue.getMainFeature().getId())
                        .name(issue.getMainFeature().getName())
                        .domain(issue.getMainFeature().getDomain())
                        .build())
                .assignedTo(issue.getAssignedTo() != null ? UserSummary.builder()
                        .id(issue.getAssignedTo().getId())
                        .username(issue.getAssignedTo().getUsername())
                        .fullName(issue.getAssignedTo().getUsername())
                        .build() : null)
                .owner(issue.getOwner() != null ? UserSummary.builder()
                        .id(issue.getOwner().getId())
                        .username(issue.getOwner().getUsername())
                        .fullName(issue.getOwner().getUsername())
                        .build() : null)
                .createdBy(issue.getCreatedBy())
                .resultComment(issue.getResultComment())
                .attachmentUrl(issue.getAttachmentUrl())
                .resolvedAt(issue.getResolvedAt())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}
