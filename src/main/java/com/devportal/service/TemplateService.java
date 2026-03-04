package com.devportal.service;

import com.devportal.domain.entity.Template;
import com.devportal.domain.entity.User;
import com.devportal.domain.enums.TemplateEntityType;
import com.devportal.dto.request.TemplateRequest;
import com.devportal.dto.response.TemplateResponse;
import com.devportal.dto.response.UserSummary;
import com.devportal.repository.TemplateRepository;
import com.devportal.repository.UserRepository;
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
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;

    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TemplateResponse> getTemplatesByType(TemplateEntityType entityType) {
        return templateRepository.findByEntityTypeAndIsActiveTrue(entityType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TemplateResponse getTemplateById(UUID id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));
        return mapToResponse(template);
    }

    public TemplateResponse getDefaultTemplate(TemplateEntityType entityType) {
        return templateRepository.findByEntityTypeAndIsDefaultTrue(entityType)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        User currentUser = getCurrentUser();

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            templateRepository.findByEntityTypeAndIsDefaultTrue(request.getEntityType())
                    .ifPresent(t -> {
                        t.setIsDefault(false);
                        templateRepository.save(t);
                    });
        }

        Template template = Template.builder()
                .name(request.getName())
                .description(request.getDescription())
                .entityType(request.getEntityType())
                .templateData(request.getTemplateData())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .createdBy(currentUser)
                .build();

        return mapToResponse(templateRepository.save(template));
    }

    @Transactional
    public TemplateResponse updateTemplate(UUID id, TemplateRequest request) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(template.getIsDefault())) {
            templateRepository.findByEntityTypeAndIsDefaultTrue(request.getEntityType())
                    .ifPresent(t -> {
                        t.setIsDefault(false);
                        templateRepository.save(t);
                    });
        }

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTemplateData(request.getTemplateData());
        template.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        return mapToResponse(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));
        template.setIsActive(false);
        templateRepository.save(template);
    }

    private TemplateResponse mapToResponse(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .entityType(template.getEntityType())
                .templateData(template.getTemplateData())
                .isDefault(template.getIsDefault())
                .isActive(template.getIsActive())
                .createdBy(template.getCreatedBy() != null ? UserSummary.builder()
                        .id(template.getCreatedBy().getId())
                        .username(template.getCreatedBy().getUsername())
                        .build() : null)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
