package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.dto.request.UserNoteRequest;
import com.devportal.dto.response.PagedResponse;
import com.devportal.dto.response.UserNoteResponse;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNoteService {

    private final UserNoteRepository noteRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ReleaseRepository releaseRepository;
    private final UtilityRepository utilityRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserNoteResponse> getMyNotes(int page, int size, String search, boolean archived) {
        UUID userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<UserNote> notePage;
        if (search != null && !search.isEmpty()) {
            notePage = noteRepository.searchByUserIdAndKeyword(userId, search, pageable);
        } else if (archived) {
            notePage = noteRepository.findByUserIdAndIsArchivedTrueOrderByUpdatedAtDesc(userId, pageable);
        } else {
            notePage = noteRepository.findByUserIdAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(userId, pageable);
        }

        return PagedResponse.from(notePage.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<UserNoteResponse> getPinnedNotes() {
        UUID userId = getCurrentUserId();
        return noteRepository.findByUserIdAndIsPinnedTrueAndIsArchivedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserNoteResponse getNoteById(UUID id) {
        UserNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
        validateOwnership(note);
        return toResponse(note);
    }

    @Transactional
    public UserNoteResponse createNote(UserNoteRequest request) {
        User user = getCurrentUser();

        UserNote note = UserNote.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .tags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null)
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .isArchived(false)
                .moduleType(request.getModuleType())
                .moduleId(request.getModuleId())
                .build();

        note = noteRepository.save(note);
        log.info("Note created: {} by user {}", note.getTitle(), user.getUsername());
        return toResponse(note);
    }

    @Transactional
    public UserNoteResponse updateNote(UUID id, UserNoteRequest request) {
        UserNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
        validateOwnership(note);

        note.setTitle(request.getTitle());
        note.setDescription(request.getDescription());
        note.setTags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null);
        if (request.getIsPinned() != null) {
            note.setIsPinned(request.getIsPinned());
        }
        if (request.getIsArchived() != null) {
            note.setIsArchived(request.getIsArchived());
        }
        note.setModuleType(request.getModuleType());
        note.setModuleId(request.getModuleId());

        note = noteRepository.save(note);
        log.info("Note updated: {}", note.getTitle());
        return toResponse(note);
    }

    @Transactional
    public void deleteNote(UUID id) {
        UserNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
        validateOwnership(note);
        noteRepository.delete(note);
        log.info("Note deleted: {}", note.getTitle());
    }

    @Transactional
    public UserNoteResponse togglePin(UUID id) {
        UserNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
        validateOwnership(note);
        note.setIsPinned(!note.getIsPinned());
        note = noteRepository.save(note);
        return toResponse(note);
    }

    @Transactional
    public UserNoteResponse toggleArchive(UUID id) {
        UserNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
        validateOwnership(note);
        note.setIsArchived(!note.getIsArchived());
        note = noteRepository.save(note);
        return toResponse(note);
    }

    private UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private void validateOwnership(UserNote note) {
        UUID currentUserId = getCurrentUserId();
        if (!note.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Note not found");
        }
    }

    private UserNoteResponse toResponse(UserNote note) {
        return UserNoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .tags(note.getTags() != null ? Arrays.asList(note.getTags()) : null)
                .isPinned(note.getIsPinned())
                .isArchived(note.getIsArchived())
                .moduleType(note.getModuleType())
                .moduleId(note.getModuleId())
                .moduleName(getModuleName(note.getModuleType(), note.getModuleId()))
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private String getModuleName(String moduleType, UUID moduleId) {
        if (moduleType == null || moduleId == null) return null;
        
        return switch (moduleType.toUpperCase()) {
            case "ISSUE" -> issueRepository.findById(moduleId).map(Issue::getTitle).orElse(null);
            case "MICROSERVICE" -> microserviceRepository.findById(moduleId).map(Microservice::getName).orElse(null);
            case "RELEASE" -> releaseRepository.findById(moduleId).map(Release::getName).orElse(null);
            case "UTILITY" -> utilityRepository.findById(moduleId).map(Utility::getTitle).orElse(null);
            case "INCIDENT" -> incidentRepository.findById(moduleId).map(Incident::getTitle).orElse(null);
            case "HOTFIX" -> hotfixRepository.findById(moduleId).map(Hotfix::getTitle).orElse(null);
            case "FEATURE" -> featureRepository.findById(moduleId).map(Feature::getName).orElse(null);
            default -> null;
        };
    }
}
