package com.devportal.domain.entity;

import com.devportal.domain.enums.ReminderPriority;
import com.devportal.domain.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "reminder_datetime", nullable = false)
    private LocalDateTime reminderDatetime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReminderPriority priority = ReminderPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "module_type", length = 50)
    private String moduleType;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "is_system_generated")
    @Builder.Default
    private Boolean isSystemGenerated = false;

    @Column(name = "snoozed_until")
    private LocalDateTime snoozedUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
