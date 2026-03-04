package com.devportal.service;

import com.devportal.domain.entity.UtilityCategory;
import com.devportal.dto.request.UtilityCategoryRequest;
import com.devportal.dto.response.UtilityCategoryResponse;
import com.devportal.repository.UtilityCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityCategoryService {

    private final UtilityCategoryRepository categoryRepository;

    public List<UtilityCategoryResponse> getAllCategories() {
        return categoryRepository.findAllOrderBySortOrder().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UtilityCategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrder().stream()
                .map(this::mapToResponseWithChildren)
                .collect(Collectors.toList());
    }

    public UtilityCategoryResponse getCategoryById(UUID id) {
        UtilityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return mapToResponseWithChildren(category);
    }

    @Transactional
    public UtilityCategoryResponse createCategory(UtilityCategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        UtilityCategory parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
        }

        UtilityCategory category = UtilityCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public UtilityCategoryResponse updateCategory(UUID id, UtilityCategoryRequest request) {
        UtilityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        categoryRepository.findByName(request.getName())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Category with this name already exists");
                });

        UtilityCategory parent = null;
        if (request.getParentId() != null && !request.getParentId().equals(id)) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParent(parent);
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(UUID id) {
        UtilityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        
        if (!category.getUtilities().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with utilities");
        }
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        categoryRepository.delete(category);
    }

    private UtilityCategoryResponse mapToResponse(UtilityCategory category) {
        return UtilityCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .utilityCount(category.getUtilities() != null ? category.getUtilities().size() : 0)
                .build();
    }

    private UtilityCategoryResponse mapToResponseWithChildren(UtilityCategory category) {
        UtilityCategoryResponse response = mapToResponse(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            response.setChildren(category.getChildren().stream()
                    .map(this::mapToResponseWithChildren)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}
