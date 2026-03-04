package com.devportal.service;

import com.devportal.domain.entity.ActivityLog;
import com.devportal.domain.entity.User;
import com.devportal.dto.response.ActivityLogResponse;
import com.devportal.mapper.ActivityLogMapper;
import com.devportal.repository.ActivityLogRepository;
import com.devportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final UserRepository userRepository;

    @Transactional
    public void logActivity(User user, String action, String entityType, UUID entityId, String description) {
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(getClientIpAddress())
                .build();
        activityLogRepository.save(activityLog);
    }

    @Transactional
    public void logActivity(String action, String entityType, UUID entityId, String description) {
        User user = getCurrentUser();
        logActivity(user, action, entityType, entityId, description);
    }

    @Transactional
    public void logActivity(String action, String entityType, UUID entityId, String description, String oldValue, String newValue) {
        User user = getCurrentUser();
        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(getClientIpAddress())
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getRecentActivities(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> activities = activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return activityLogMapper.toResponseList(activities.getContent());
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAllActivities(Pageable pageable) {
        Page<ActivityLog> activities = activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return activities.map(activityLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getActivitiesByEntityType(String entityType, Pageable pageable) {
        Page<ActivityLog> activities = activityLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
        return activities.map(activityLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getActivitiesByAction(String action, Pageable pageable) {
        Page<ActivityLog> activities = activityLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
        return activities.map(activityLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getActivitiesByUser(UUID userId, Pageable pageable) {
        Page<ActivityLog> activities = activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return activities.map(activityLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> searchActivities(String entityType, String action, UUID userId, Pageable pageable) {
        if (entityType != null && action != null && userId != null) {
            return activityLogRepository.findByEntityTypeAndActionAndUserIdOrderByCreatedAtDesc(entityType, action, userId, pageable)
                    .map(activityLogMapper::toResponse);
        } else if (entityType != null && action != null) {
            return activityLogRepository.findByEntityTypeAndActionOrderByCreatedAtDesc(entityType, action, pageable)
                    .map(activityLogMapper::toResponse);
        } else if (entityType != null) {
            return getActivitiesByEntityType(entityType, pageable);
        } else if (action != null) {
            return getActivitiesByAction(action, pageable);
        } else if (userId != null) {
            return getActivitiesByUser(userId, pageable);
        }
        return getAllActivities(pageable);
    }

    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP address", e);
        }
        return null;
    }
}
