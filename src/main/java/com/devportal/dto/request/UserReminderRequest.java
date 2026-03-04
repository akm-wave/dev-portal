package com.devportal.dto.request;

import com.devportal.domain.enums.ReminderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UserReminderRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title must not exceed 300 characters")
    private String title;

    private String description;

    @NotNull(message = "Reminder date/time is required")
    private LocalDateTime reminderDatetime;

    private ReminderPriority priority;

    private String moduleType;

    private UUID moduleId;
}
