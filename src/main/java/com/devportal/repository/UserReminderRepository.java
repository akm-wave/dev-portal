package com.devportal.repository;

import com.devportal.domain.entity.UserReminder;
import com.devportal.domain.enums.ReminderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserReminderRepository extends JpaRepository<UserReminder, UUID> {

    Page<UserReminder> findByUserIdOrderByReminderDatetimeAsc(UUID userId, Pageable pageable);

    Page<UserReminder> findByUserIdAndStatusOrderByReminderDatetimeAsc(UUID userId, ReminderStatus status, Pageable pageable);

    List<UserReminder> findByUserIdAndStatusAndReminderDatetimeBetweenOrderByReminderDatetimeAsc(
            UUID userId, ReminderStatus status, LocalDateTime start, LocalDateTime end);

    List<UserReminder> findByUserIdAndReminderDatetimeBetweenOrderByReminderDatetimeAsc(
            UUID userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM UserReminder r WHERE r.user.id = :userId AND r.status = 'PENDING' " +
           "AND r.reminderDatetime < :now ORDER BY r.reminderDatetime ASC")
    List<UserReminder> findOverdueReminders(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM UserReminder r WHERE r.user.id = :userId AND r.status = 'PENDING' " +
           "AND r.reminderDatetime >= :start AND r.reminderDatetime <= :end ORDER BY r.reminderDatetime ASC")
    List<UserReminder> findTodayReminders(@Param("userId") UUID userId, 
                                          @Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);

    @Query("SELECT r FROM UserReminder r WHERE r.user.id = :userId AND r.status = 'PENDING' " +
           "AND r.reminderDatetime > :end ORDER BY r.reminderDatetime ASC")
    List<UserReminder> findUpcomingReminders(@Param("userId") UUID userId, @Param("end") LocalDateTime end);

    List<UserReminder> findByUserIdAndModuleTypeAndModuleId(UUID userId, String moduleType, UUID moduleId);

    @Query("SELECT r FROM UserReminder r WHERE r.status = 'PENDING' AND r.reminderDatetime < :now")
    List<UserReminder> findAllOverdueReminders(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserReminder r SET r.status = 'OVERDUE' WHERE r.status = 'PENDING' AND r.reminderDatetime < :now")
    int markOverdueReminders(@Param("now") LocalDateTime now);

    long countByUserIdAndStatus(UUID userId, ReminderStatus status);

    long countByUserIdAndStatusAndReminderDatetimeBefore(UUID userId, ReminderStatus status, LocalDateTime datetime);

    boolean existsByUserIdAndModuleTypeAndModuleIdAndIsSystemGeneratedTrue(UUID userId, String moduleType, UUID moduleId);
}
