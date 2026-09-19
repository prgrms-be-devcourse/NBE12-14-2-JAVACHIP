package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetUpdateRequest(

        @NotNull(message = "예산 변경 금액 입력은 필수입니다.")
        @Positive(message = "예산 변경 금액은 음수 또는 0이 될 수 없습니다.")
        Long totalBudget,

        @NotNull(message = "예산 변경 타입은 필수입니다.")
        BudgetType budgetType,
        String reason
) {
}
