package com.budzet.domain.budget.dto;

import java.time.LocalDateTime;

public record BudgetRequestResponse(
        Long id,
        Long roomId,
        Long userId,
        String reason,
        Long requestedAmount,
        String status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
