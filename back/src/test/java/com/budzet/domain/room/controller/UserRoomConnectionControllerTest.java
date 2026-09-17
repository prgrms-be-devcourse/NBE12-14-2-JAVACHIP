package com.budzet.domain.room.controller;

import com.budzet.domain.room.dto.MemberResponse;
import com.budzet.domain.room.entity.Authority;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.room.service.UserRoomConnectionService;
import com.budzet.global.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRoomConnectionControllerTest {

    @Test
    @DisplayName("멤버 권한 조회 성공")
    void getAuthority_success() {

        UserRoomConnectionService service =
                mock(UserRoomConnectionService.class);

        UserRoomConnectionController controller =
                new UserRoomConnectionController(service);

        UserRoomConnection connection =
                mock(UserRoomConnection.class);

        when(connection.getAuthority())
                .thenReturn(Authority.MEMBER);

        when(service.getConnection(1L, 2L))
                .thenReturn(connection);

        ApiResponse<String> response =
                controller.getAuthority(1L, 2L);

        assertEquals(200, response.resultCode());
        assertEquals("멤버 권한 조회 성공", response.message());
        assertEquals("MEMBER", response.data());
    }

    @Test
    @DisplayName("모임 멤버 목록 조회 성공")
    void getMembers_success() {

        UserRoomConnectionService service =
                mock(UserRoomConnectionService.class);

        UserRoomConnectionController controller =
                new UserRoomConnectionController(service);

        List<MemberResponse> members = List.of(
                new MemberResponse(1L, "홍길동", Authority.OWNER),
                new MemberResponse(2L, "김철수", Authority.MEMBER)
        );

        when(service.getMembers(1L))
                .thenReturn(members);

        ApiResponse<List<MemberResponse>> response =
                controller.getMembers(1L);

        assertEquals(200, response.resultCode());
        assertEquals("멤버 목록 조회 성공", response.message());
        assertEquals(2, response.data().size());
        assertEquals("홍길동", response.data().get(0).name());
        assertEquals(Authority.OWNER, response.data().get(0).authority());
    }

    @Test
    @DisplayName("멤버 강퇴 성공")
    void kickMember_success() {

        UserRoomConnectionService service =
                mock(UserRoomConnectionService.class);

        UserRoomConnectionController controller =
                new UserRoomConnectionController(service);

        controller.kickMember(1L, 2L);

        verify(service)
                .kickMember(1L, 2L);
    }

    @Test
    @DisplayName("모임 탈퇴 성공")
    void leaveRoom_success() {

        UserRoomConnectionService service =
                mock(UserRoomConnectionService.class);

        UserRoomConnectionController controller =
                new UserRoomConnectionController(service);

        controller.leaveRoom(1L);

        verify(service)
                .leaveRoom(1L, 2L);
    }


}