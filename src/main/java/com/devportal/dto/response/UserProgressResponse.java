package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressResponse {
    private String userId;
    private String username;
    private int totalTasks;
    private int completedTasks;
    private int inProgressTasks;
    private int blockedTasks;
    private int pendingTasks;
    private int totalWeight;
    private int completedWeight;
    private int progress;
    private String progressLevel;
    private String emoji;
}
