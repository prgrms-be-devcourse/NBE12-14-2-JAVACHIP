package com.budzet.domain.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BudgetChangeCreateRequest(

        @NotNull(message = "정산금액 입력은 필수입니다.")
        @Positive(message = "정산금액은 음수 또는 0이 될 수 없습니다.")
        Long changedBudget,
        @Size(max = 40, message = "변동 사유는 최대 40자까지 입력 가능합니다.")
        String changeReason
) {
}
