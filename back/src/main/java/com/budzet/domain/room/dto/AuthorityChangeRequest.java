package com.budzet.domain.room.dto;

import com.budzet.domain.room.entity.Authority;
import jakarta.validation.constraints.NotNull;

public record AuthorityChangeRequest(
        @NotNull
        Authority authority
) {
}