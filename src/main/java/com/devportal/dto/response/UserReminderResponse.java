package com.devportal.dto.response;

import com.devportal.domain.enums.ReminderPriority;
import com.devportal.domain.enums.ReminderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReminderResponse {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime reminderDatetime;
    private ReminderPriority priority;
    private ReminderStatus status;
    private String moduleType;
    private UUID moduleId;
    private String moduleName;
    private Boolean isSystemGenerated;
    private LocalDateTime snoozedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
