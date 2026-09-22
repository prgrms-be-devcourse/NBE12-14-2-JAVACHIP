package com.budzet.domain.budget.dto;

import jakarta.validation.constraints.*;

public record BudgetChangeUpdateRequest(

        @NotNull(message = "신청 금액은 필수입니다.")
        @Positive(message = "정산금액은 음수 또는 0이 될 수 없습니다.")
        Long changeBudget,

        @NotNull(message = "정산 금액은 필수입니다.")
        @Positive(message = "정산금액은 음수 또는 0이 될 수 없습니다.")
        Long changedBudget,

        @NotBlank(message = "유저 이름은 필수입니다.")
        String userName,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String userEmail,

        @NotBlank(message = "예산 변동 사유는 필수입니다.")
        @Size(max = 20, message = "예산 변동 사유는 최대 20자까지 입력 가능합니다.")
        String reason
) {}
