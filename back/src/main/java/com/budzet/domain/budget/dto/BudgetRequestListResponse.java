package com.budzet.domain.budget.dto;

import java.time.LocalDateTime;

public record BudgetRequestListResponse(
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
