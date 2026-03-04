package com.devportal.controller;

import com.devportal.domain.enums.ReminderStatus;
import com.devportal.dto.request.UserReminderRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.dto.response.UserReminderResponse;
import com.devportal.service.UserReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/workspace/reminders")
@RequiredArgsConstructor
public class UserReminderController {

    private final UserReminderService reminderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserReminderResponse>>> getMyReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReminderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.getMyReminders(page, size, status)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<UserReminderResponse>>> getTodayReminders() {
        return ResponseEntity.ok(ApiResponse.success(reminderService.getTodayReminders()));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<UserReminderResponse>>> getOverdueReminders() {
        return ResponseEntity.ok(ApiResponse.success(reminderService.getOverdueReminders()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<UserReminderResponse>>> getUpcomingReminders() {
        return ResponseEntity.ok(ApiResponse.success(reminderService.getUpcomingReminders()));
    }

    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getReminderCounts() {
        Map<String, Long> counts = Map.of(
                "overdue", reminderService.getOverdueCount(),
                "pending", reminderService.getPendingCount()
        );
        return ResponseEntity.ok(ApiResponse.success(counts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserReminderResponse>> getReminderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.getReminderById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserReminderResponse>> createReminder(
            @Valid @RequestBody UserReminderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.createReminder(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserReminderResponse>> updateReminder(
            @PathVariable UUID id,
            @Valid @RequestBody UserReminderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.updateReminder(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable UUID id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<UserReminderResponse>> markAsCompleted(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.markAsCompleted(id)));
    }

    @PostMapping("/{id}/snooze")
    public ResponseEntity<ApiResponse<UserReminderResponse>> snoozeReminder(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "15") int minutes) {
        return ResponseEntity.ok(ApiResponse.success(reminderService.snoozeReminder(id, minutes)));
    }
}
