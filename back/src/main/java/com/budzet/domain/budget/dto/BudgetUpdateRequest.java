package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetUpdateRequest(

        @NotNull(message = "예산 변동 금액은 입력은 필수입니다.")
        @Positive(message = "예산 변동 금액은 0원 보다 커야 합니다.")
        Long totalBudget,

        @NotNull(message = "예산 변동 유형은 필수입니다.")
        BudgetType budgetType,
        String changeReason
) {}
