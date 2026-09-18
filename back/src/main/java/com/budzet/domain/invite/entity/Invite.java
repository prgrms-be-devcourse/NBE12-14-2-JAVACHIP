package com.budzet.domain.invite.entity;

import com.budzet.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Invite {
    @Id
    @Column(length = 10)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expireAt;

    public Invite(String code, Room room, LocalDateTime expireAt) {
        this.code = code;
        this.room = room;
        this.expireAt = expireAt;
    }
}
