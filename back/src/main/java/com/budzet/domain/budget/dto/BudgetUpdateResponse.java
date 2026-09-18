package com.budzet.domain.budget.dto;

import com.budzet.domain.room.entity.Room;

public record BudgetUpdateResponse(
        Long roomId,
        Long totalBudget
){
    public static BudgetUpdateResponse from(Room room){
        return new BudgetUpdateResponse(
                room.getId(),
                room.getTotalBudget()
        );
    }
}
