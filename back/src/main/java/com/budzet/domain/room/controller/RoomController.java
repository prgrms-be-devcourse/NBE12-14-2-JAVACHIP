package com.budzet.domain.room.controller;

import com.budzet.domain.room.dto.*;
import com.budzet.domain.room.service.RoomService;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "02 - 1. 모임방", description = "모임방 생성, 목록/상세 조회, 정보 수정, 삭제 및 권한 위임/지정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final Rq rq;

    @Operation(
            summary = "모임 생성",
            description = "새로운 모임을 생성, 생성한 사용자는 모임장이 된다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
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

    @Operation(
            summary = "모임 목록 조회",
            description = "참여중인 모임 전체 조회",
            security = @SecurityRequirement(name = "BearerAuth")
    )
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

    @Operation(
            summary = "모임 상세 조회",
            security = @SecurityRequirement(name = "BearerAuth")
    )
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

    @Operation(
            summary = "모임 정보 수정",
            description = "모임장 권한 필요",
            security = @SecurityRequirement(name = "BearerAuth")
    )
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

    @Operation(
            summary = "모임 삭제",
            description = "모임장 권한 필요",
            security = @SecurityRequirement(name = "BearerAuth")
    )
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

    @Operation(
            summary = "운영자 지정/해제",
            description = "모임장 권한 필요",
            security = @SecurityRequirement(name = "BearerAuth")
    )
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

    @Operation(
            summary = "모임장 권한 위임",
            description = "모임장 권한 필요",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PatchMapping("/{roomId}/members/{userId}/owner")
    public ResponseEntity<ApiResponse<Void>> delegateOwner(
            @PathVariable Long roomId,
            @PathVariable Long userId
    ) {
        Long actorId = rq.getActor().getId();

        roomService.delegateOwner(
                actorId,
                roomId,
                userId
        );

        return ApiResponse.response(
                HttpStatus.OK,
                "모임장 권한이 위임되었습니다.",
                null
        );
    }
}
