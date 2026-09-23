package com.budzet.domain.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BudgetRequestModifyRequest(
        @NotBlank(message = "신청사유는 필수입니다.")
        @Size(max = 20, message = "반려사유는 50자 이하여야 합니다.")
        String reason,

        @NotNull(message = "신청예산은 필수입니다.")
        @Positive(message = "신청예산은 음수 또는 0이 될 수 없습니다.")
        Long rerequestedAmount
) {

}