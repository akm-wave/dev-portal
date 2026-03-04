package com.devportal.controller;

import com.devportal.domain.entity.User;
import com.devportal.dto.response.UserProgressResponse;
import com.devportal.repository.UserRepository;
import com.devportal.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProgressResponse> getMyProgress() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        UserProgressResponse progress = progressService.getUserProgress(user.getId());
        return ResponseEntity.ok(progress);
    }
}
