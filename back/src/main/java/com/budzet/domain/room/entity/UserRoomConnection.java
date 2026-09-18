package com.budzet.domain.room.entity;

import com.budzet.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@IdClass(UserRoomConnectionId.class)
@EntityListeners(AuditingEntityListener.class)
public class UserRoomConnection {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Authority authority;

    private boolean joined;

    @CreatedDate
    private LocalDateTime createdAt;

    private UserRoomConnection(User user, Room room, Authority authority, boolean joined) {
        this.user = user;
        this.room = room;
        this.authority = authority;
        this.joined = joined;
    }

    public static UserRoomConnection createOwner(User user, Room room) {
        return new UserRoomConnection(user, room, Authority.OWNER, true);
    }
}
