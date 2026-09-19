package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;

import java.time.LocalDateTime;
import java.util.List;

public record BudgetChangeListResponse(
        List<ListItem> list
){
    public static BudgetChangeListResponse from(List<BudgetChange> budgetChanges){

        List<ListItem> listItems = budgetChanges.stream()
                .map(ListItem::from)
                .toList();
        return new BudgetChangeListResponse(listItems);
    }

    public record ListItem(
            Long id,
            Long changeBudget,
            BudgetType type,
            String userName,
            String reason,
            LocalDateTime processedAt){

        public static ListItem from(BudgetChange budgetChange){

            return new ListItem(
                    budgetChange.getId(),
                    budgetChange.getChangedBudget(),
                    budgetChange.getType(),
                    budgetChange.getUserName(),
                    budgetChange.getReason(),
                    budgetChange.getCreatedAt()
            );
        }
    }
}