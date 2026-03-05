package com.devportal.mapper;

import com.devportal.domain.entity.Feature;
import com.devportal.domain.entity.User;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.request.FeatureRequest;
import com.devportal.dto.response.FeatureResponse;
import com.devportal.dto.response.UserSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FeatureMapper {
    
    @Mapping(target = "microservices", ignore = true)
    @Mapping(target = "microserviceCount", constant = "0")
    @Mapping(target = "totalChecklistCount", constant = "0")
    @Mapping(target = "completedChecklistCount", constant = "0")
    @Mapping(target = "progressPercentage", constant = "0.0")
    @Mapping(target = "owner", source = "owner")
    FeatureResponse toResponse(Feature feature);
    
    List<FeatureResponse> toResponseList(List<Feature> features);
    
    @Mapping(target = "microservices", ignore = true)
    @Mapping(target = "owner", ignore = true)
    Feature toEntity(FeatureRequest request);
    
    @Mapping(target = "microservices", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updateEntity(FeatureRequest request, @MappingTarget Feature feature);
    
    default UserSummary toUserSummary(User user) {
        if (user == null) return null;
        try {
            return UserSummary.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
    
    default int safeMicroserviceCount(Feature feature) {
        try {
            return feature.getMicroservices() != null ? feature.getMicroservices().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    default int safeCountTotalChecklists(Feature feature) {
        try {
            if (feature.getMicroservices() == null) return 0;
            return feature.getMicroservices().stream()
                    .mapToInt(m -> m.getChecklists() != null ? m.getChecklists().size() : 0)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }
    
    default int safeCountCompletedChecklists(Feature feature) {
        try {
            if (feature.getMicroservices() == null) return 0;
            return feature.getMicroservices().stream()
                    .flatMap(m -> m.getChecklists() != null ? m.getChecklists().stream() : java.util.stream.Stream.empty())
                    .filter(c -> c.getStatus() == ChecklistStatus.COMPLETED)
                    .mapToInt(c -> 1)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }
    
    default double safeCalculateProgress(Feature feature) {
        try {
            int total = safeCountTotalChecklists(feature);
            if (total == 0) return 0.0;
            int completed = safeCountCompletedChecklists(feature);
            return Math.round((completed * 100.0 / total) * 100.0) / 100.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
