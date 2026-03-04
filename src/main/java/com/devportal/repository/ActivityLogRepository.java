package com.devportal.repository;

import com.devportal.domain.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    Page<ActivityLog> findByUserId(UUID userId, Pageable pageable);
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);
    Page<ActivityLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<ActivityLog> findByEntityTypeAndActionOrderByCreatedAtDesc(String entityType, String action, Pageable pageable);
    Page<ActivityLog> findByEntityTypeAndActionAndUserIdOrderByCreatedAtDesc(String entityType, String action, UUID userId, Pageable pageable);
}
