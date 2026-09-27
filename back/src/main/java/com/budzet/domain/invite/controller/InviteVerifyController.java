package com.budzet.domain.invite.controller;

import com.budzet.domain.invite.dto.InviteResponse;
import com.budzet.domain.invite.service.InviteService;
import com.budzet.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "04. 초대", description = "초대 링크 생성, 목록 조회, 코드 참여 및 유효성 검증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/invites")
public class InviteVerifyController {

    private final InviteService inviteService;

    @Operation(summary = "초대 링크 검증")
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