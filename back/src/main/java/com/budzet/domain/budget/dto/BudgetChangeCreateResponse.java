package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;

import java.time.LocalDateTime;

public record BudgetChangeCreateResponse(
        Long id,
        Long requestId,
        Long userId,
        Long changeBudget,
        Long changedBudget,
        BudgetType budgetType,
        String userName,
        LocalDateTime processedAt,
        String reason
    ) {
    public static BudgetChangeCreateResponse from(BudgetChange budgetChange){
        return new BudgetChangeCreateResponse(
                budgetChange.getId(),
                budgetChange.getRequest().getId(),
                budgetChange.getUser().getId(),
                budgetChange.getChangeBudget(),
                budgetChange.getChangedBudget(),
                budgetChange.getType(),
                budgetChange.getUserName(),
                budgetChange.getCreatedAt(),
                budgetChange.getReason()
        );
    }
}
