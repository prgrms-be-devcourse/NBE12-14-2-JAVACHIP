package com.budzet.domain.budget.dto;

public record BudgetRequestRequest(
        String reason,
        Long requested_amount
) {

}
