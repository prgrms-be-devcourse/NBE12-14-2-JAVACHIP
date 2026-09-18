package com.budzet.domain.invite.dto;

import java.time.LocalDateTime;

public record InviteResponse(
        String code,
        LocalDateTime expireAt
) {
}