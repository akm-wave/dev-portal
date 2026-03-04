package com.devportal.mapper;

import com.devportal.domain.entity.Microservice;
import com.devportal.domain.entity.User;
import com.devportal.dto.request.MicroserviceRequest;
import com.devportal.dto.response.MicroserviceResponse;
import com.devportal.dto.response.UserSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ChecklistMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MicroserviceMapper {
    
    @Mapping(target = "checklists", source = "checklists")
    @Mapping(target = "checklistCount", expression = "java(microservice.getChecklists() != null ? microservice.getChecklists().size() : 0)")
    @Mapping(target = "completedChecklistCount", expression = "java(countCompletedChecklists(microservice))")
    @Mapping(target = "progressPercentage", expression = "java(calculateProgress(microservice))")
    @Mapping(target = "featureCount", expression = "java(microservice.getFeatures() != null ? microservice.getFeatures().size() : 0)")
    @Mapping(target = "owner", expression = "java(toUserSummary(microservice.getOwner()))")
    MicroserviceResponse toResponse(Microservice microservice);
    
    List<MicroserviceResponse> toResponseList(List<Microservice> microservices);
    
    @Mapping(target = "checklists", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "owner", ignore = true)
    Microservice toEntity(MicroserviceRequest request);
    
    @Mapping(target = "checklists", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updateEntity(MicroserviceRequest request, @MappingTarget Microservice microservice);
    
    default UserSummary toUserSummary(User user) {
        if (user == null) return null;
        return UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
    
    default int countCompletedChecklists(Microservice microservice) {
        if (microservice.getChecklists() == null) return 0;
        return (int) microservice.getChecklists().stream()
                .filter(c -> c.getStatus() == com.devportal.domain.enums.ChecklistStatus.DONE)
                .count();
    }
    
    default double calculateProgress(Microservice microservice) {
        if (microservice.getChecklists() == null || microservice.getChecklists().isEmpty()) return 0.0;
        int total = microservice.getChecklists().size();
        int completed = countCompletedChecklists(microservice);
        return Math.round((completed * 100.0 / total) * 100.0) / 100.0;
    }
}
