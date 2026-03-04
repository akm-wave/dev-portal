package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ReminderPriority;
import com.devportal.domain.enums.ReminderStatus;
import com.devportal.dto.request.UserReminderRequest;
import com.devportal.dto.response.PagedResponse;
import com.devportal.dto.response.UserReminderResponse;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReminderService {

    private final UserReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ReleaseRepository releaseRepository;
    private final UtilityRepository utilityRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserReminderResponse> getMyReminders(int page, int size, ReminderStatus status) {
        UUID userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<UserReminder> reminderPage;
        if (status != null) {
            reminderPage = reminderRepository.findByUserIdAndStatusOrderByReminderDatetimeAsc(userId, status, pageable);
        } else {
            reminderPage = reminderRepository.findByUserIdOrderByReminderDatetimeAsc(userId, pageable);
        }

        return PagedResponse.from(reminderPage.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<UserReminderResponse> getTodayReminders() {
        UUID userId = getCurrentUserId();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        return reminderRepository.findTodayReminders(userId, startOfDay, endOfDay)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserReminderResponse> getOverdueReminders() {
        UUID userId = getCurrentUserId();
        return reminderRepository.findOverdueReminders(userId, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserReminderResponse> getUpcomingReminders() {
        UUID userId = getCurrentUserId();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        return reminderRepository.findUpcomingReminders(userId, endOfToday)
                .stream()
                .limit(10)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserReminderResponse getReminderById(UUID id) {
        UserReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        validateOwnership(reminder);
        return toResponse(reminder);
    }

    @Transactional
    public UserReminderResponse createReminder(UserReminderRequest request) {
        User user = getCurrentUser();

        UserReminder reminder = UserReminder.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .reminderDatetime(request.getReminderDatetime())
                .priority(request.getPriority() != null ? request.getPriority() : ReminderPriority.MEDIUM)
                .status(ReminderStatus.PENDING)
                .moduleType(request.getModuleType())
                .moduleId(request.getModuleId())
                .isSystemGenerated(false)
                .build();

        reminder = reminderRepository.save(reminder);
        log.info("Reminder created: {} by user {}", reminder.getTitle(), user.getUsername());
        return toResponse(reminder);
    }

    @Transactional
    public UserReminderResponse updateReminder(UUID id, UserReminderRequest request) {
        UserReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        validateOwnership(reminder);

        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setReminderDatetime(request.getReminderDatetime());
        if (request.getPriority() != null) {
            reminder.setPriority(request.getPriority());
        }
        reminder.setModuleType(request.getModuleType());
        reminder.setModuleId(request.getModuleId());

        reminder = reminderRepository.save(reminder);
        log.info("Reminder updated: {}", reminder.getTitle());
        return toResponse(reminder);
    }

    @Transactional
    public void deleteReminder(UUID id) {
        UserReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        validateOwnership(reminder);
        reminderRepository.delete(reminder);
        log.info("Reminder deleted: {}", reminder.getTitle());
    }

    @Transactional
    public UserReminderResponse markAsCompleted(UUID id) {
        UserReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        validateOwnership(reminder);
        reminder.setStatus(ReminderStatus.COMPLETED);
        reminder = reminderRepository.save(reminder);
        return toResponse(reminder);
    }

    @Transactional
    public UserReminderResponse snoozeReminder(UUID id, int minutes) {
        UserReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        validateOwnership(reminder);
        reminder.setStatus(ReminderStatus.SNOOZED);
        reminder.setSnoozedUntil(LocalDateTime.now().plusMinutes(minutes));
        reminder.setReminderDatetime(LocalDateTime.now().plusMinutes(minutes));
        reminder = reminderRepository.save(reminder);
        return toResponse(reminder);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void updateOverdueReminders() {
        int updated = reminderRepository.markOverdueReminders(LocalDateTime.now());
        if (updated > 0) {
            log.info("Marked {} reminders as overdue", updated);
        }
    }

    @Transactional(readOnly = true)
    public long getOverdueCount() {
        UUID userId = getCurrentUserId();
        return reminderRepository.countByUserIdAndStatus(userId, ReminderStatus.OVERDUE);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        UUID userId = getCurrentUserId();
        return reminderRepository.countByUserIdAndStatus(userId, ReminderStatus.PENDING);
    }

    private UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private void validateOwnership(UserReminder reminder) {
        UUID currentUserId = getCurrentUserId();
        if (!reminder.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Reminder not found");
        }
    }

    private UserReminderResponse toResponse(UserReminder reminder) {
        return UserReminderResponse.builder()
                .id(reminder.getId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .reminderDatetime(reminder.getReminderDatetime())
                .priority(reminder.getPriority())
                .status(reminder.getStatus())
                .moduleType(reminder.getModuleType())
                .moduleId(reminder.getModuleId())
                .moduleName(getModuleName(reminder.getModuleType(), reminder.getModuleId()))
                .isSystemGenerated(reminder.getIsSystemGenerated())
                .snoozedUntil(reminder.getSnoozedUntil())
                .createdAt(reminder.getCreatedAt())
                .updatedAt(reminder.getUpdatedAt())
                .build();
    }

    private String getModuleName(String moduleType, UUID moduleId) {
        if (moduleType == null || moduleId == null) return null;
        
        return switch (moduleType.toUpperCase()) {
            case "ISSUE" -> issueRepository.findById(moduleId).map(Issue::getTitle).orElse(null);
            case "MICROSERVICE" -> microserviceRepository.findById(moduleId).map(Microservice::getName).orElse(null);
            case "RELEASE" -> releaseRepository.findById(moduleId).map(Release::getName).orElse(null);
            case "UTILITY" -> utilityRepository.findById(moduleId).map(Utility::getTitle).orElse(null);
            case "INCIDENT" -> incidentRepository.findById(moduleId).map(Incident::getTitle).orElse(null);
            case "HOTFIX" -> hotfixRepository.findById(moduleId).map(Hotfix::getTitle).orElse(null);
            case "FEATURE" -> featureRepository.findById(moduleId).map(Feature::getName).orElse(null);
            default -> null;
        };
    }
}
