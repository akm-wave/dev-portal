package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type;
    private String username;
    private String email;
    private String role;
    
    public static AuthResponse of(String token, String username, String email, String role) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(username)
                .email(email)
                .role(role)
                .build();
    }
}
