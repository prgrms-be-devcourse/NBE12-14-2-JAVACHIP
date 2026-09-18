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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    void createInviteByOwner() {
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

        verify(inviteRepository)
                .save(any(Invite.class));
    }

    @Test
    void createInviteByMember() {
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

    @Test
    void verifyValidInvite() {
        // given
        String token = "ABC1234567";

        Invite invite = mock(Invite.class);

        when(inviteRepository.findById(token))
                .thenReturn(Optional.of(invite));

        when(invite.getExpireAt())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(invite.getCode())
                .thenReturn(token);

        // when
        InviteResponse response =
                inviteService.verifyInvite(token);

        // then
        assertThat(response).isNotNull();
        assertThat(response.code())
                .isEqualTo(token);
        assertThat(response.expireAt())
                .isNotNull();

        verify(inviteRepository)
                .findById(token);
    }

    @Test
    void verifyNonExistentInvite() {
        // given
        String token = "NOTFOUND";

        when(inviteRepository.findById(token))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                inviteService.verifyInvite(token)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.NOT_FOUND
                );

        verify(inviteRepository)
                .findById(token);
    }

    @Test
    void verifyExpiredInvite() {
        // given
        String token = "EXPIRED123";

        Invite invite = mock(Invite.class);

        when(inviteRepository.findById(token))
                .thenReturn(Optional.of(invite));

        when(invite.getExpireAt())
                .thenReturn(LocalDateTime.now().minusHours(1));

        // when & then
        assertThatThrownBy(() ->
                inviteService.verifyInvite(token)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.CONFLICT
                );

        verify(inviteRepository)
                .findById(token);
    }
}