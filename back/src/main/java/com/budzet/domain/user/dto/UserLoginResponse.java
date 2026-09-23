package com.budzet.domain.user.dto;

import com.budzet.domain.user.entity.User;

public record UserLoginResponse(
        UserDto user,
        String accessToken,
        String refreshToken
) {
    public static UserLoginResponse from(User user, String accessToken, String refreshToken){
        return new UserLoginResponse(
                new UserDto(user),
                accessToken,
                refreshToken
        );
    }
}
