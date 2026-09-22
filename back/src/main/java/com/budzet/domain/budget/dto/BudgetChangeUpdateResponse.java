package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetChange;

public record BudgetChangeUpdateResponse(
        Long id,
        Long changeBudget,
        Long changedBudget,
        String userName,
        String userEmail,
        String reason
) {
    public static BudgetChangeUpdateResponse from(BudgetChange budgetChange){
        return new BudgetChangeUpdateResponse(
                budgetChange.getId(),
                budgetChange.getChangeBudget(),
                budgetChange.getChangedBudget(),
                budgetChange.getUserName(),
                budgetChange.getUser().getEmail(),
                budgetChange.getReason()
        );
    }
}
