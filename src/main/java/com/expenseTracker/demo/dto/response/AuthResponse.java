package com.expenseTracker.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type;
    private UUID userId;
    private String email;
    private String fullName;
    private String role;

    public static AuthResponse of(String token, UUID userId, String email, String fullName, String role) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();
    }
}
