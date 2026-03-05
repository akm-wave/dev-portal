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
    
    @Mapping(target = "checklists", ignore = true)
    @Mapping(target = "checklistCount", constant = "0")
    @Mapping(target = "completedChecklistCount", constant = "0")
    @Mapping(target = "progressPercentage", constant = "0.0")
    @Mapping(target = "featureCount", constant = "0")
    @Mapping(target = "owner", source = "owner")
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
    
    default int countCompletedChecklists(Microservice microservice) {
        if (microservice.getChecklists() == null) return 0;
        return (int) microservice.getChecklists().stream()
                .filter(c -> c.getStatus() == com.devportal.domain.enums.ChecklistStatus.COMPLETED)
                .count();
    }
    
    default double calculateProgress(Microservice microservice) {
        if (microservice.getChecklists() == null || microservice.getChecklists().isEmpty()) return 0.0;
        int total = microservice.getChecklists().size();
        int completed = countCompletedChecklists(microservice);
        return Math.round((completed * 100.0 / total) * 100.0) / 100.0;
    }
}
