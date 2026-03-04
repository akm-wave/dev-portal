package com.devportal.service;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.enums.ChecklistPriority;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.request.ChecklistRequest;
import com.devportal.dto.response.ChecklistResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.exception.BadRequestException;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.mapper.ChecklistMapper;
import com.devportal.repository.ChecklistRepository;
import com.devportal.repository.MicroserviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ChecklistMapper checklistMapper;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PagedResponse<ChecklistResponse> getAllChecklists(int page, int size, String sortBy, String sortDir,
                                                              ChecklistStatus status, ChecklistPriority priority, String search) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Checklist> checklistPage;
        
        if (search != null && !search.isEmpty()) {
            checklistPage = checklistRepository.searchByNameOrDescription(search, pageable);
        } else if (status != null && priority != null) {
            checklistPage = checklistRepository.findByIsActiveTrueAndStatusAndPriority(status, priority, pageable);
        } else if (status != null) {
            checklistPage = checklistRepository.findByIsActiveTrueAndStatus(status, pageable);
        } else if (priority != null) {
            checklistPage = checklistRepository.findByIsActiveTrueAndPriority(priority, pageable);
        } else {
            checklistPage = checklistRepository.findByIsActiveTrue(pageable);
        }

        Page<ChecklistResponse> responsePage = checklistPage.map(checklistMapper::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public ChecklistResponse getChecklistById(UUID id) {
        Checklist checklist = checklistRepository.findById(id)
                .filter(Checklist::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", id));
        return checklistMapper.toResponse(checklist);
    }

    @Transactional
    public ChecklistResponse createChecklist(ChecklistRequest request) {
        Checklist checklist = checklistMapper.toEntity(request);
        checklist.setCreatedBy(getCurrentUsername());
        checklist.setIsActive(true);
        
        if (checklist.getStatus() == null) {
            checklist.setStatus(ChecklistStatus.PENDING);
        }
        if (checklist.getPriority() == null) {
            checklist.setPriority(ChecklistPriority.MEDIUM);
        }

        checklist = checklistRepository.save(checklist);
        log.info("Checklist created: {}", checklist.getName());
        activityLogService.logActivity("CREATE", "CHECKLIST", checklist.getId(), "Created checklist: " + checklist.getName());
        return checklistMapper.toResponse(checklist);
    }

    @Transactional
    public ChecklistResponse updateChecklist(UUID id, ChecklistRequest request) {
        Checklist checklist = checklistRepository.findById(id)
                .filter(Checklist::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", id));

        checklistMapper.updateEntity(request, checklist);
        checklist = checklistRepository.save(checklist);
        log.info("Checklist updated: {}", checklist.getName());
        activityLogService.logActivity("UPDATE", "CHECKLIST", checklist.getId(), "Updated checklist: " + checklist.getName());
        return checklistMapper.toResponse(checklist);
    }

    @Transactional
    public void deleteChecklist(UUID id) {
        Checklist checklist = checklistRepository.findById(id)
                .filter(Checklist::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", id));

        // Check if checklist is linked to any microservice
        if (!microserviceRepository.findByChecklistId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete checklist that is linked to active microservices");
        }

        // Soft delete
        checklist.setIsActive(false);
        checklistRepository.save(checklist);
        log.info("Checklist soft deleted: {}", checklist.getName());
        activityLogService.logActivity("DELETE", "CHECKLIST", id, "Deleted checklist: " + checklist.getName());
    }

    @Transactional
    public ChecklistResponse updateStatus(UUID id, ChecklistStatus status) {
        Checklist checklist = checklistRepository.findById(id)
                .filter(Checklist::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", id));

        checklist.setStatus(status);
        checklist = checklistRepository.save(checklist);
        log.info("Checklist status updated: {} -> {}", checklist.getName(), status);
        activityLogService.logActivity("UPDATE_STATUS", "CHECKLIST", checklist.getId(), "Updated checklist status: " + checklist.getName() + " -> " + status);
        return checklistMapper.toResponse(checklist);
    }

    @Transactional(readOnly = true)
    public List<ChecklistResponse> getChecklistsByIds(List<UUID> ids) {
        List<Checklist> checklists = checklistRepository.findByIdInAndIsActiveTrue(ids);
        return checklistMapper.toResponseList(checklists);
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
