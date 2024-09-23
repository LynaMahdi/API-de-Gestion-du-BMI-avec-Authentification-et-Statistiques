package com.spring.jwt.entity;

import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    String token;
    String refreshToken;

    public void setToken(String token) {
        this.token = token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
