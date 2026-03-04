package com.devportal.service;

import com.devportal.domain.entity.User;
import com.devportal.domain.entity.Utility;
import com.devportal.domain.entity.UtilityVersion;
import com.devportal.dto.response.UserSummary;
import com.devportal.dto.response.UtilityVersionResponse;
import com.devportal.repository.UserRepository;
import com.devportal.repository.UtilityRepository;
import com.devportal.repository.UtilityVersionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityVersionService {

    private final UtilityVersionRepository versionRepository;
    private final UtilityRepository utilityRepository;
    private final UserRepository userRepository;

    public List<UtilityVersionResponse> getVersionHistory(UUID utilityId) {
        return versionRepository.findByUtilityIdOrderByVersionNumberDesc(utilityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UtilityVersionResponse getVersion(UUID utilityId, Integer versionNumber) {
        UtilityVersion version = versionRepository.findByUtilityIdAndVersionNumber(utilityId, versionNumber)
                .orElseThrow(() -> new EntityNotFoundException("Version not found"));
        return mapToResponse(version);
    }

    public UtilityVersionResponse getCurrentVersion(UUID utilityId) {
        return versionRepository.findCurrentVersion(utilityId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional
    public UtilityVersionResponse createVersion(UUID utilityId, String changeSummary) {
        Utility utility = utilityRepository.findById(utilityId)
                .orElseThrow(() -> new EntityNotFoundException("Utility not found"));
        User currentUser = getCurrentUser();

        versionRepository.findByUtilityIdAndIsCurrent(utilityId, true)
                .ifPresent(v -> {
                    v.setIsCurrent(false);
                    versionRepository.save(v);
                });

        Integer maxVersion = versionRepository.findMaxVersionNumber(utilityId).orElse(0);
        Integer newVersionNumber = maxVersion + 1;

        UtilityVersion version = UtilityVersion.builder()
                .utility(utility)
                .versionNumber(newVersionNumber)
                .title(utility.getTitle())
                .description(utility.getDescription())
                .content(utility.getContent())
                .changeSummary(changeSummary)
                .createdBy(currentUser)
                .isCurrent(true)
                .build();

        utility.setCurrentVersion(newVersionNumber);
        utilityRepository.save(utility);

        return mapToResponse(versionRepository.save(version));
    }

    @Transactional
    public UtilityVersionResponse revertToVersion(UUID utilityId, Integer versionNumber) {
        Utility utility = utilityRepository.findById(utilityId)
                .orElseThrow(() -> new EntityNotFoundException("Utility not found"));
        UtilityVersion targetVersion = versionRepository.findByUtilityIdAndVersionNumber(utilityId, versionNumber)
                .orElseThrow(() -> new EntityNotFoundException("Version not found"));

        utility.setTitle(targetVersion.getTitle());
        utility.setDescription(targetVersion.getDescription());
        utility.setContent(targetVersion.getContent());
        utilityRepository.save(utility);

        return createVersion(utilityId, "Reverted to version " + versionNumber);
    }

    public String compareVersions(UUID utilityId, Integer version1, Integer version2) {
        UtilityVersion v1 = versionRepository.findByUtilityIdAndVersionNumber(utilityId, version1)
                .orElseThrow(() -> new EntityNotFoundException("Version " + version1 + " not found"));
        UtilityVersion v2 = versionRepository.findByUtilityIdAndVersionNumber(utilityId, version2)
                .orElseThrow(() -> new EntityNotFoundException("Version " + version2 + " not found"));

        StringBuilder diff = new StringBuilder();
        diff.append("=== Version ").append(version1).append(" vs Version ").append(version2).append(" ===\n\n");

        if (!v1.getTitle().equals(v2.getTitle())) {
            diff.append("Title changed:\n");
            diff.append("- ").append(v1.getTitle()).append("\n");
            diff.append("+ ").append(v2.getTitle()).append("\n\n");
        }

        if (!safeEquals(v1.getDescription(), v2.getDescription())) {
            diff.append("Description changed:\n");
            diff.append("- ").append(v1.getDescription() != null ? v1.getDescription() : "(empty)").append("\n");
            diff.append("+ ").append(v2.getDescription() != null ? v2.getDescription() : "(empty)").append("\n\n");
        }

        if (!safeEquals(v1.getContent(), v2.getContent())) {
            diff.append("Content changed:\n");
            diff.append("Version ").append(version1).append(" content length: ")
                    .append(v1.getContent() != null ? v1.getContent().length() : 0).append(" chars\n");
            diff.append("Version ").append(version2).append(" content length: ")
                    .append(v2.getContent() != null ? v2.getContent().length() : 0).append(" chars\n");
        }

        return diff.toString();
    }

    private boolean safeEquals(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }

    private UtilityVersionResponse mapToResponse(UtilityVersion version) {
        return UtilityVersionResponse.builder()
                .id(version.getId())
                .utilityId(version.getUtility().getId())
                .versionNumber(version.getVersionNumber())
                .title(version.getTitle())
                .description(version.getDescription())
                .content(version.getContent())
                .changeSummary(version.getChangeSummary())
                .createdBy(version.getCreatedBy() != null ? UserSummary.builder()
                        .id(version.getCreatedBy().getId())
                        .username(version.getCreatedBy().getUsername())
                        .build() : null)
                .createdAt(version.getCreatedAt())
                .isCurrent(version.getIsCurrent())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
