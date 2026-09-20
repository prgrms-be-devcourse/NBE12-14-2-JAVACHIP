package com.budzet.domain.budget.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BudgetChangeUpdateRequest(
        // todo 유저,이유만 변경? 금액도 변경?
        @Positive(message = "정산금액은 음수 또는 0이 될 수 없습니다.")
        Long changeBudget,

        @Positive(message = "정산금액은 음수 또는 0이 될 수 없습니다.")
        Long changedBudget,

        @Size(max = 40, message = "변동 사유는 최대 40자까지 입력 가능합니다.")
        String changeReason,

        String userName,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String userEmail,

        String reason
) {}
