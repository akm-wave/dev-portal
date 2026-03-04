package com.devportal.service;

import com.devportal.domain.entity.User;
import com.devportal.domain.enums.Role;
import com.devportal.dto.request.LoginRequest;
import com.devportal.dto.request.RegisterRequest;
import com.devportal.dto.response.AuthResponse;
import com.devportal.exception.BadRequestException;
import com.devportal.repository.UserRepository;
import com.devportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ActivityLogService activityLogService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        String token = tokenProvider.generateToken(user.getUsername());
        
        activityLogService.logActivity(user, "USER_REGISTERED", "User", user.getId(), 
                "New user registered: " + user.getUsername());

        return AuthResponse.of(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = tokenProvider.generateToken(authentication);
        log.info("User logged in successfully: {}", user.getUsername());

        activityLogService.logActivity(user, "USER_LOGIN", "User", user.getId(), 
                "User logged in: " + user.getUsername());

        return AuthResponse.of(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
