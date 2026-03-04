package com.devportal.controller;

import com.devportal.domain.entity.User;
import com.devportal.domain.enums.Role;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.UserSummary;
import com.devportal.exception.ResourceNotFoundException;
import com.devportal.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<User>>> getAll(
            Pageable pageable,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) String role) {
        Page<User> users;
        if (approved != null) {
            users = userRepository.findByApproved(approved, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(Role.valueOf(role), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/approved")
    @Operation(summary = "Get all approved users for assignment dropdowns")
    public ResponseEntity<ApiResponse<List<UserSummary>>> getApprovedUsers() {
        List<UserSummary> users = userRepository.findByApprovedTrue().stream()
                .map(u -> UserSummary.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Approved users retrieved", users));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending approval users")
    public ResponseEntity<ApiResponse<List<User>>> getPendingUsers() {
        List<User> users = userRepository.findByApprovedFalse();
        return ResponseEntity.ok(ApiResponse.success("Pending users retrieved", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<User>> getById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user (admin only)")
    public ResponseEntity<ApiResponse<User>> create(@RequestBody Map<String, String> body) {
        User user = User.builder()
                .username(body.get("username"))
                .email(body.get("email"))
                .password(passwordEncoder.encode(body.get("password")))
                .fullName(body.get("fullName"))
                .role(body.containsKey("role") ? Role.valueOf(body.get("role")) : Role.USER)
                .approved(true) // Admin-created users are auto-approved
                .build();
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        if (body.containsKey("username")) user.setUsername(body.get("username"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("fullName")) user.setFullName(body.get("fullName"));
        if (body.containsKey("role")) user.setRole(Role.valueOf(body.get("role")));
        if (body.containsKey("password") && !body.get("password").isEmpty()) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }
        
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a user registration")
    public ResponseEntity<ApiResponse<User>> approveUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setApproved(true);
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User approved successfully", user));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject/revoke a user")
    public ResponseEntity<ApiResponse<User>> rejectUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setApproved(false);
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User rejected successfully", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
