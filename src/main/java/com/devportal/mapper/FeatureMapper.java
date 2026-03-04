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

@Mapper(componentModel = "spring", uses = {MicroserviceMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FeatureMapper {
    
    @Mapping(target = "microservices", source = "microservices")
    @Mapping(target = "microserviceCount", expression = "java(feature.getMicroservices() != null ? feature.getMicroservices().size() : 0)")
    @Mapping(target = "totalChecklistCount", expression = "java(countTotalChecklists(feature))")
    @Mapping(target = "completedChecklistCount", expression = "java(countCompletedChecklists(feature))")
    @Mapping(target = "progressPercentage", expression = "java(calculateProgress(feature))")
    @Mapping(target = "owner", expression = "java(toUserSummary(feature.getOwner()))")
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
        return UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
    
    default int countTotalChecklists(Feature feature) {
        if (feature.getMicroservices() == null) return 0;
        return feature.getMicroservices().stream()
                .mapToInt(m -> m.getChecklists() != null ? m.getChecklists().size() : 0)
                .sum();
    }
    
    default int countCompletedChecklists(Feature feature) {
        if (feature.getMicroservices() == null) return 0;
        return feature.getMicroservices().stream()
                .flatMap(m -> m.getChecklists() != null ? m.getChecklists().stream() : java.util.stream.Stream.empty())
                .filter(c -> c.getStatus() == ChecklistStatus.DONE)
                .mapToInt(c -> 1)
                .sum();
    }
    
    default double calculateProgress(Feature feature) {
        int total = countTotalChecklists(feature);
        if (total == 0) return 0.0;
        int completed = countCompletedChecklists(feature);
        return Math.round((completed * 100.0 / total) * 100.0) / 100.0;
    }
}
