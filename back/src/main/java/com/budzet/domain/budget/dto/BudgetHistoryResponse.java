package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetChange;

import java.time.LocalDateTime;
import java.util.List;

public record BudgetHistoryResponse(
        List<HistoryItem> history
) {

    public static BudgetHistoryResponse from(
            List<BudgetChange> budgetChanges
    ){
        List<HistoryItem> historyItems = budgetChanges
                .stream()
                .map(HistoryItem::from)
                .toList();

        return new BudgetHistoryResponse(historyItems);
    }

    public record HistoryItem(
            Long id,
            Long changeBudget,
            Long changedBudget,
            String type,
            String userName,
            String reason,
            LocalDateTime processedAt
            ){
        public static HistoryItem from(BudgetChange budgetChange){
            return new HistoryItem(
                    budgetChange.getId(),
                    budgetChange.getChangeBudget(),
                    budgetChange.getChangedBudget(),
                    budgetChange.getType().name(),
                    budgetChange.getUserName(),
                    budgetChange.getReason(),
                    budgetChange.getCreatedAt()
            );
        }
    }
}

