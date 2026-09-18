package com.budzet.domain.invite.service;

import com.budzet.domain.invite.dto.InviteResponse;
import com.budzet.domain.invite.entity.Invite;
import com.budzet.domain.invite.repository.InviteRepository;
import com.budzet.domain.room.entity.*;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRoomConnectionRepository userRoomConnectionRepository;

    @InjectMocks
    private InviteService inviteService;

    @Test
    void OWNER_CAN_CREATE_LINK() {
        // given
        Long roomId = 1L;
        Long userId = 1L;

        Room room = mock(Room.class);

        UserRoomConnection connection =
                mock(UserRoomConnection.class);

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(userRoomConnectionRepository
                .findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(connection));

        when(connection.getAuthority())
                .thenReturn(Authority.OWNER);

        // when
        InviteResponse response =
                inviteService.createInvite(roomId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.code()).isNotNull();
        assertThat(response.code()).hasSize(10);
        assertThat(response.expireAt()).isNotNull();

        verify(inviteRepository).save(any(Invite.class));
    }

    @Test
    void MEMBER_CANNOT_CREATE_LINK() {
        // given
        Long roomId = 1L;
        Long userId = 2L;

        Room room = mock(Room.class);

        UserRoomConnection connection =
                mock(UserRoomConnection.class);

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(userRoomConnectionRepository
                .findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(connection));

        when(connection.getAuthority())
                .thenReturn(Authority.MEMBER);

        // when & then
        assertThatThrownBy(() ->
                inviteService.createInvite(roomId, userId)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.UNAUTHORIZED
                );

        verify(inviteRepository, never())
                .save(any(Invite.class));
    }
}