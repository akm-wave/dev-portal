package com.devportal.service;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.entity.User;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.dto.response.UserProgressResponse;
import com.devportal.repository.ChecklistRepository;
import com.devportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ChecklistRepository checklistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProgressResponse getUserProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Checklist> assignedChecklists = checklistRepository.findByAssignedToIdAndIsActiveTrue(userId);

        int totalTasks = assignedChecklists.size();
        int completedTasks = 0;
        int inProgressTasks = 0;
        int blockedTasks = 0;
        int pendingTasks = 0;
        int totalWeight = 0;
        int completedWeight = 0;

        for (Checklist checklist : assignedChecklists) {
            int weight = checklist.getWeight() != null ? checklist.getWeight() : 1;
            totalWeight += weight;

            switch (checklist.getStatus()) {
                case COMPLETED:
                    completedTasks++;
                    completedWeight += weight;
                    break;
                case IN_PROGRESS:
                    inProgressTasks++;
                    break;
                case BLOCKED:
                    blockedTasks++;
                    break;
                case PLANNED:
                default:
                    pendingTasks++;
                    break;
            }
        }

        int progress = totalWeight > 0 ? (completedWeight * 100) / totalWeight : 0;
        String progressLevel = getProgressLevel(progress);
        String emoji = getProgressEmoji(progress);

        return UserProgressResponse.builder()
                .userId(userId.toString())
                .username(user.getUsername())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .blockedTasks(blockedTasks)
                .pendingTasks(pendingTasks)
                .totalWeight(totalWeight)
                .completedWeight(completedWeight)
                .progress(progress)
                .progressLevel(progressLevel)
                .emoji(emoji)
                .build();
    }

    private String getProgressLevel(int progress) {
        if (progress >= 100) return "TASK_MASTER";
        if (progress >= 60) return "FOCUS_MODE";
        if (progress >= 20) return "CODING";
        return "IDLE";
    }

    private String getProgressEmoji(int progress) {
        if (progress >= 100) return "🏆";
        if (progress >= 60) return "⚡";
        if (progress >= 20) return "👨‍💻";
        return "😴";
    }
}
