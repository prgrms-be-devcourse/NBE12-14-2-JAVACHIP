package com.budzet.domain.budget.dto;

import jakarta.validation.constraints.Size;

public record BudgetRequestRejectRequest(
        @Size(max = 50, message = "반려사유는 50자 이하여야 합니다.")
        String rejectReason
) {

}
