package com.devportal.mapper;

import com.devportal.domain.entity.Checklist;
import com.devportal.dto.request.ChecklistRequest;
import com.devportal.dto.response.ChecklistResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChecklistMapper {
    
    ChecklistResponse toResponse(Checklist checklist);
    
    List<ChecklistResponse> toResponseList(List<Checklist> checklists);
    
    Checklist toEntity(ChecklistRequest request);
    
    void updateEntity(ChecklistRequest request, @MappingTarget Checklist checklist);
}
