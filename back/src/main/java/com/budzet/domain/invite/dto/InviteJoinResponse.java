package com.budzet.domain.invite.dto;

import com.budzet.domain.room.entity.UserRoomConnection;

import java.time.LocalDateTime;

public record InviteJoinResponse(
        Long userId,
        Long roomId,
        String authority,
        boolean joined,
        LocalDateTime createdAt
) {
    public static InviteJoinResponse from(UserRoomConnection connection) {
        return new InviteJoinResponse(
                connection.getUser().getId(),
                connection.getRoom().getId(),
                connection.getAuthority().name(),
                connection.isJoined(),
                connection.getCreatedAt()
        );
    }
}
