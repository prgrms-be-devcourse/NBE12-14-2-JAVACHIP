package com.budzet.domain.invite.service;

import com.budzet.domain.invite.dto.InviteResponse;
import com.budzet.domain.invite.entity.Invite;
import com.budzet.domain.invite.repository.InviteRepository;
import com.budzet.domain.room.entity.Authority;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final RoomRepository roomRepository;
    private final UserRoomConnectionRepository userRoomConnectionRepository;

    @Transactional
    public InviteResponse createInvite(Long roomId, Long userId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow();

        UserRoomConnection connection =
                userRoomConnectionRepository
                        .findByUser_IdAndRoom_Id(userId, roomId)
                        .orElseThrow();

        if (connection.getAuthority() != Authority.OWNER) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String code = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        LocalDateTime expireAt = LocalDateTime.now()
                .plusHours(1);

        Invite invite = new Invite(
                code,
                room,
                expireAt
        );

        inviteRepository.save(invite);

        return new InviteResponse(
                invite.getCode(),
                invite.getExpireAt()
        );
    }

    @Transactional(readOnly = true)
    public InviteResponse verifyInvite(String token) {

        Invite invite = inviteRepository.findById(token)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOT_FOUND)
                );

        if (!invite.getExpireAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        return new InviteResponse(
                invite.getCode(),
                invite.getExpireAt()
        );
    }
}