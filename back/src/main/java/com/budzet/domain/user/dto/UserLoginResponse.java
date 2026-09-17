package com.budzet.domain.user.dto;

import com.budzet.domain.user.entity.User;

public record UserLoginResponse(
        UserDto user,
        String accessToken
) {
    public static UserLoginResponse from(User user, String accessToken){
        return new UserLoginResponse(
                new UserDto(user),
                accessToken
        );
    }
}
