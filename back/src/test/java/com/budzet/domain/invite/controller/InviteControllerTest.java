package com.budzet.domain.invite.controller;

import com.budzet.domain.invite.dto.InviteResponse;
import com.budzet.domain.invite.service.InviteService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InviteControllerTest {

    private InviteService inviteService;
    private Rq rq;
    private InviteController controller;

    @BeforeEach
    void setUp() {
        inviteService = mock(InviteService.class);
        rq = mock(Rq.class);

        controller = new InviteController(
                inviteService,
                rq
        );
    }

    @Test
    void CREATE_INVITE_LINK() {
        // given
        Long roomId = 1L;
        Long userId = 2L;

        User user = mock(User.class);

        InviteResponse inviteResponse =
                new InviteResponse(
                        "ABC1234567",
                        LocalDateTime.now().plusHours(1)
                );

        when(rq.getActor())
                .thenReturn(user);

        when(user.getId())
                .thenReturn(userId);

        when(inviteService.createInvite(roomId, userId))
                .thenReturn(inviteResponse);

        // when
        var response =
                controller.createInvite(roomId);

        // then
        assertThat(response.resultCode())
                .isEqualTo(201);

        assertThat(response.message())
                .isEqualTo("초대 링크 생성 성공");

        assertThat(response.data())
                .isEqualTo(inviteResponse);

        verify(rq)
                .getActor();

        verify(user)
                .getId();

        verify(inviteService)
                .createInvite(roomId, userId);
    }
}