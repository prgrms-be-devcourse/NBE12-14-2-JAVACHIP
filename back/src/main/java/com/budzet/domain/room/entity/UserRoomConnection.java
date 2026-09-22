package com.budzet.domain.room.entity;

import com.budzet.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    private UserRoomConnection(
            User user,
            Room room,
            Authority authority,
            boolean joined
    ) {
        this.user = user;
        this.room = room;
        this.authority = authority;
        this.joined = joined;
    }

    public static UserRoomConnection createOwner(
            User user,
            Room room
    ) {
        return new UserRoomConnection(
                user,
                room,
                Authority.OWNER,
                true
        );
    }

    public static UserRoomConnection createMember(
            User user,
            Room room
    ) {
        return new UserRoomConnection(
                user,
                room,
                Authority.MEMBER,
                true
        );
    }

    public void changeAuthority(Authority authority) {
        this.authority = authority;
    }
}