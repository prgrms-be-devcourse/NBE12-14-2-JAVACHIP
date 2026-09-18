package com.budzet.domain.room.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budzet.domain.room.dto.RoomCreateRequest;
import com.budzet.domain.room.dto.RoomCreateResponse;
import com.budzet.domain.room.dto.RoomDetailResponse;
import com.budzet.domain.room.dto.RoomListResponse;
import com.budzet.domain.room.dto.RoomUpdateRequest;
import com.budzet.domain.room.entity.Currency;
import com.budzet.domain.room.service.RoomService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.rq.Rq;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private Rq rq;

    @Test
    @DisplayName("모임 생성 요청 시 실제 HTTP 상태 201과 ApiResponse를 반환한다.")
    void createRoom_returnsCreatedResponse() throws Exception {
        User actor = actor(1L);
        RoomCreateRequest request = new RoomCreateRequest("사진동아리", 100_000L, Currency.KRW);
        RoomCreateResponse response = new RoomCreateResponse(
                10L, "사진동아리", 100_000L, 100_000L, "KRW", LocalDateTime.now()
        );
        when(rq.getActor()).thenReturn(actor);
        when(roomService.createRoom(eq(1L), eq(request))).thenReturn(response);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value(201))
                .andExpect(jsonPath("$.message").value("모임이 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(10));

        verify(roomService).createRoom(1L, request);
    }

    @Test
    @DisplayName("인증 사용자의 모임 목록을 조회한다.")
    void getRooms_usesAuthenticatedUserId() throws Exception {
        User actor = actor(1L);
        RoomListResponse response = new RoomListResponse(List.of());
        when(rq.getActor()).thenReturn(actor);
        when(roomService.getRooms(1L)).thenReturn(response);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.data.rooms").isEmpty());

        verify(roomService).getRooms(1L);
    }

    @Test
    @DisplayName("인증 사용자의 모임 상세 정보를 조회한다.")
    void getRoom_usesAuthenticatedUserId() throws Exception {
        User actor = actor(1L);
        RoomDetailResponse response = roomDetailResponse();
        when(rq.getActor()).thenReturn(actor);
        when(roomService.getRoom(1L, 10L)).thenReturn(response);

        mockMvc.perform(get("/rooms/{roomId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.data.id").value(10));

        verify(roomService).getRoom(1L, 10L);
    }

    @Test
    @DisplayName("모임 이름 수정 시 인증 사용자의 ID를 전달한다.")
    void updateRoom_usesAuthenticatedUserId() throws Exception {
        User actor = actor(1L);
        RoomUpdateRequest request = new RoomUpdateRequest("수정된 이름");
        RoomDetailResponse response = roomDetailResponse();
        when(rq.getActor()).thenReturn(actor);
        when(roomService.updateRoom(eq(1L), eq(10L), eq(request))).thenReturn(response);

        mockMvc.perform(patch("/rooms/{roomId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200));

        verify(roomService).updateRoom(1L, 10L, request);
    }

    private User actor(Long userId) {
        User actor = org.mockito.Mockito.mock(User.class);
        when(actor.getId()).thenReturn(userId);
        return actor;
    }

    private RoomDetailResponse roomDetailResponse() {
        return new RoomDetailResponse(
                10L, "사진동아리", 100_000L, 100_000L, "KRW", LocalDateTime.now()
        );
    }
}
