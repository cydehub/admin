package com.stockzeno.wms.auth.dto;

import java.util.Set;
import java.util.UUID;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessTokenExpiresInSeconds;
    private UUID userId;
    private String email;
    private Set<String> roles;

    public AuthResponse(String accessToken,
                        String refreshToken,
                        String tokenType,
                        long accessTokenExpiresInSeconds,
                        UUID userId,
                        String email,
                        Set<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiresInSeconds;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
