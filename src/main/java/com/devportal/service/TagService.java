package com.devportal.service;

import com.devportal.domain.entity.Tag;
import com.devportal.dto.request.TagRequest;
import com.devportal.dto.response.TagResponse;
import com.devportal.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        return tagRepository.findAllOrderByName().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TagResponse getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        return mapToResponse(tag);
    }

    public List<TagResponse> searchTags(String query) {
        return tagRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }

        Tag tag = Tag.builder()
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#5b6cf0")
                .description(request.getDescription())
                .build();

        return mapToResponse(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        tagRepository.findByName(request.getName())
                .filter(t -> !t.getId().equals(id))
                .ifPresent(t -> {
                    throw new IllegalArgumentException("Tag with this name already exists");
                });

        tag.setName(request.getName());
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        tag.setDescription(request.getDescription());

        return mapToResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        tagRepository.delete(tag);
    }

    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .utilityCount(tag.getUtilities() != null ? tag.getUtilities().size() : 0)
                .build();
    }
}
