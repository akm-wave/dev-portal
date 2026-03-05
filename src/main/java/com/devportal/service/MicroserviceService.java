package com.devportal.service;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.entity.Microservice;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.domain.enums.MicroserviceStatus;
import com.devportal.dto.request.MicroserviceRequest;
import com.devportal.dto.response.MicroserviceResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.exception.BadRequestException;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.mapper.MicroserviceMapper;
import com.devportal.repository.ChecklistRepository;
import com.devportal.repository.MicroserviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicroserviceService {

    private final MicroserviceRepository microserviceRepository;
    private final ChecklistRepository checklistRepository;
    private final MicroserviceMapper microserviceMapper;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PagedResponse<MicroserviceResponse> getAllMicroservices(int page, int size, String sortBy, String sortDir,
                                                                    MicroserviceStatus status, String search) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Microservice> microservicePage;
        
        if (search != null && !search.isEmpty()) {
            microservicePage = microserviceRepository.searchByNameOrDescription(search, pageable);
        } else if (status != null) {
            microservicePage = microserviceRepository.findByStatus(status, pageable);
        } else {
            microservicePage = microserviceRepository.findAll(pageable);
        }

        Page<MicroserviceResponse> responsePage = microservicePage.map(microserviceMapper::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public MicroserviceResponse getMicroserviceById(UUID id) {
        Microservice microservice = microserviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Microservice", "id", id));
        return microserviceMapper.toResponse(microservice);
    }

    @Transactional
    public MicroserviceResponse createMicroservice(MicroserviceRequest request) {
        if (request.getChecklistIds() == null || request.getChecklistIds().isEmpty()) {
            throw new BadRequestException("At least one checklist is required");
        }

        List<Checklist> checklists = checklistRepository.findByIdInAndIsActiveTrue(request.getChecklistIds());
        if (checklists.size() != request.getChecklistIds().size()) {
            throw new BadRequestException("One or more checklists not found or inactive");
        }

        Microservice microservice = microserviceMapper.toEntity(request);
        microservice.setChecklists(new HashSet<>(checklists));
        
        if (microservice.getStatus() == null) {
            microservice.setStatus(MicroserviceStatus.PLANNED);
        }

        microservice = microserviceRepository.save(microservice);
        log.info("Microservice created: {}", microservice.getName());
        activityLogService.logActivity("CREATE", "MICROSERVICE", microservice.getId(), "Created microservice: " + microservice.getName());
        return microserviceMapper.toResponse(microservice);
    }

    @Transactional
    public MicroserviceResponse updateMicroservice(UUID id, MicroserviceRequest request) {
        Microservice microservice = microserviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Microservice", "id", id));

        if (request.getChecklistIds() == null || request.getChecklistIds().isEmpty()) {
            throw new BadRequestException("At least one checklist is required");
        }

        List<Checklist> checklists = checklistRepository.findByIdInAndIsActiveTrue(request.getChecklistIds());
        if (checklists.size() != request.getChecklistIds().size()) {
            throw new BadRequestException("One or more checklists not found or inactive");
        }

        microserviceMapper.updateEntity(request, microservice);
        microservice.setChecklists(new HashSet<>(checklists));
        
        // Auto-update status based on checklist completion
        updateMicroserviceStatus(microservice);

        microservice = microserviceRepository.save(microservice);
        log.info("Microservice updated: {}", microservice.getName());
        activityLogService.logActivity("UPDATE", "MICROSERVICE", microservice.getId(), "Updated microservice: " + microservice.getName());
        return microserviceMapper.toResponse(microservice);
    }

    @Transactional
    public void deleteMicroservice(UUID id) {
        Microservice microservice = microserviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Microservice", "id", id));

        microserviceRepository.delete(microservice);
        activityLogService.logActivity("DELETE", "MICROSERVICE", id, "Deleted microservice: " + microservice.getName());
        log.info("Microservice deleted: {}", microservice.getName());
    }

    @Transactional(readOnly = true)
    public List<MicroserviceResponse> getMicroservicesByIds(List<UUID> ids) {
        List<Microservice> microservices = microserviceRepository.findByIdIn(ids);
        return microserviceMapper.toResponseList(microservices);
    }

    @Transactional(readOnly = true)
    public List<MicroserviceResponse> getMicroservicesByFeatureId(UUID featureId) {
        List<Microservice> microservices = microserviceRepository.findByFeatureId(featureId);
        return microserviceMapper.toResponseList(microservices);
    }

    private void updateMicroserviceStatus(Microservice microservice) {
        Set<Checklist> checklists = microservice.getChecklists();
        if (checklists == null || checklists.isEmpty()) return;

        long completedCount = checklists.stream()
                .filter(c -> c.getStatus() == ChecklistStatus.COMPLETED)
                .count();

        if (completedCount == checklists.size()) {
            microservice.setStatus(MicroserviceStatus.COMPLETED);
        } else if (completedCount > 0) {
            microservice.setStatus(MicroserviceStatus.IN_PROGRESS);
        }
    }
}
