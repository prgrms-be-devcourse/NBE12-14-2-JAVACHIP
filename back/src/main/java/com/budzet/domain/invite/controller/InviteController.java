package com.budzet.domain.invite.controller;

import com.budzet.domain.invite.dto.InviteResponse;
import com.budzet.domain.invite.service.InviteService;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class InviteController {

    private final InviteService inviteService;
    private final Rq rq;

    @PostMapping("/{roomId}/invites")
    public ApiResponse<InviteResponse> createInvite(
            @PathVariable Long roomId
    ) {
        Long userId = rq.getActor().getId();

        InviteResponse response =
                inviteService.createInvite(roomId, userId);

        return ApiResponse.success(
                HttpStatus.CREATED,
                "초대 링크 생성 성공",
                response
        );
    }

    @GetMapping("/{roomId}/invites")
    public ApiResponse<List<InviteResponse>> getInvites(
            @PathVariable Long roomId
    ) {
        List<InviteResponse> response =
                inviteService.getInvites(roomId);

        return ApiResponse.success(
                HttpStatus.OK,
                "초대 링크 조회 성공",
                response
        );
    }
}