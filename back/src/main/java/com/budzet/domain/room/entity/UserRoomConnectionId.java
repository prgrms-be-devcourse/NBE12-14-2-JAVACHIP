package com.budzet.domain.room.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class UserRoomConnectionId implements Serializable {

    private Long user;
    private Long room;

    public UserRoomConnectionId(Long user, Long room) {
        this.user = user;
        this.room = room;
    }
}