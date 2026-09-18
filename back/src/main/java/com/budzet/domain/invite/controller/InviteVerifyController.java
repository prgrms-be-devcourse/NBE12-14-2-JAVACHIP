package com.budzet.domain.invite.controller;

import com.budzet.domain.invite.dto.InviteResponse;
import com.budzet.domain.invite.service.InviteService;
import com.budzet.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invites")
public class InviteVerifyController {

    private final InviteService inviteService;

    @GetMapping("/{token}")
    public ApiResponse<InviteResponse> verifyInvite(
            @PathVariable String token
    ) {
        InviteResponse response =
                inviteService.verifyInvite(token);

        return ApiResponse.success(
                HttpStatus.OK,
                "초대 링크 검증 성공",
                response
        );
    }
}