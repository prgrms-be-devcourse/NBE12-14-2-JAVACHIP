package com.budzet.domain.room.controller;

import com.budzet.domain.room.dto.RoomCreateRequest;
import com.budzet.domain.room.dto.RoomCreateResponse;
import com.budzet.domain.room.dto.RoomDetailResponse;
import com.budzet.domain.room.dto.RoomListResponse;
import com.budzet.domain.room.dto.RoomUpdateRequest;
import com.budzet.domain.room.service.RoomService;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.budzet.domain.room.dto.AuthorityChangeRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final Rq rq;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomCreateResponse>> createRoom(
            @RequestBody @Valid RoomCreateRequest request
    ) {
        Long userId = rq.getActor().getId();
        RoomCreateResponse response = roomService.createRoom(userId, request);

        return ApiResponse.response(
                HttpStatus.CREATED,
                "모임이 생성되었습니다.",
                response
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RoomListResponse>> getRooms() {
        Long userId = rq.getActor().getId();
        RoomListResponse response = roomService.getRooms(userId);

        return ApiResponse.response(
                HttpStatus.OK,
                "모임 목록을 조회했습니다.",
                response
        );
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoom(
            @PathVariable Long roomId
    ) {
        Long userId = rq.getActor().getId();
        RoomDetailResponse response = roomService.getRoom(userId, roomId);

        return ApiResponse.response(
                HttpStatus.OK,
                "모임을 조회했습니다.",
                response
        );
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> updateRoom(
            @PathVariable Long roomId,
            @RequestBody @Valid RoomUpdateRequest request
    ) {
        Long userId = rq.getActor().getId();
        RoomDetailResponse response = roomService.updateRoom(userId, roomId, request);

        return ApiResponse.response(
                HttpStatus.OK,
                "모임이 수정되었습니다.",
                response
        );
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable Long roomId
    ) {
        Long userId = rq.getActor().getId();
        roomService.deleteRoom(userId, roomId);

        return ApiResponse.response(
                HttpStatus.OK,
                "모임을 삭제했습니다.",
                null
        );
    }

    @PatchMapping("/{roomId}/members/{userId}/authority")
    public ResponseEntity<ApiResponse<Void>> changeAuthority(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestBody @Valid AuthorityChangeRequest request
    ) {
        Long actorId = rq.getActor().getId();

        roomService.changeAuthority(
                actorId,
                roomId,
                userId,
                request
        );

        return ApiResponse.response(
                HttpStatus.OK,
                "멤버 권한이 변경되었습니다.",
                null
        );
    }
}
