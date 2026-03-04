package com.devportal.mapper;

import com.devportal.domain.entity.ActivityLog;
import com.devportal.dto.response.ActivityLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {
    
    @Mapping(target = "username", source = "user.username")
    ActivityLogResponse toResponse(ActivityLog activityLog);
    
    List<ActivityLogResponse> toResponseList(List<ActivityLog> activityLogs);
}
