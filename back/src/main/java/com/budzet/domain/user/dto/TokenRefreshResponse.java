package com.budzet.domain.user.dto;

import com.budzet.domain.user.entity.User;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenRefreshResponse from(String accessToken, String refreshToken){
        return new TokenRefreshResponse(
                accessToken,
                refreshToken
        );
    }
}