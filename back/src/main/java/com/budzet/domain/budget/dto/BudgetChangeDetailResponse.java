package com.budzet.domain.budget.dto;

import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;

import java.time.LocalDateTime;

public record BudgetChangeDetailResponse(
        Long id,
        Long roomId,
        Long userId,
        Long requestId,
        Long changeBudget,
        Long changedBudget,
        String changeReason,
        BudgetType budgetType,
        LocalDateTime processedAt,
        String userName,
        String userEmail,
        String reason,
        LocalDateTime requestCreatedAt,
        LocalDateTime requestUpdatedAt
) {
    public static BudgetChangeDetailResponse from(BudgetChange budgetChange){

        // 관리자의 예산 변경 건은 예산 신청 데이터가 없기 때문에 관련 필드는 null로 초기화한 뒤 조건문으로 처리합니다.
        Long requestId = null;
        LocalDateTime requestCreatedAt = null;
        LocalDateTime requestUpdatedAt = null;
        if(budgetChange.getRequest() != null){
            requestId = budgetChange.getRequest().getId();
            requestCreatedAt = budgetChange.getRequest().getCreatedAt();
            requestUpdatedAt = budgetChange.getRequest().getUpdatedAt();
        }

        return new BudgetChangeDetailResponse(
                budgetChange.getId(),
                budgetChange.getRoom().getId(),
                budgetChange.getUser().getId(),
                requestId,
                budgetChange.getChangeBudget(),
                budgetChange.getChangedBudget(),
                budgetChange.getChangeReason(),
                budgetChange.getType(),
                budgetChange.getCreatedAt(),
                budgetChange.getUserName(),
                budgetChange.getUser().getEmail(),
                budgetChange.getReason(),
                requestCreatedAt,
                requestUpdatedAt
        );
    }
}
