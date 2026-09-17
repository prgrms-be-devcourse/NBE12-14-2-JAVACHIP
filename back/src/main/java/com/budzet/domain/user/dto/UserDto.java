package com.budzet.domain.user.dto;

import com.budzet.domain.user.entity.User;

public record UserDto (
    Long id,
    String email,
    String name
){
    public UserDto(User user){
        this(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
