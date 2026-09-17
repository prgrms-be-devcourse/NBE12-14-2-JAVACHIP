package com.budzet.domain.budget.dto;

import com.budzet.domain.room.entity.Room;

public record BudgetResponse(
        long roomId,
        String roomName,
        long totalBudget,
        long availableBudget,
        long reserveBudget,
        String currency

) {
    public static BudgetResponse from(Room room){
        return new BudgetResponse(
                room.getId(),
                room.getName(),
                room.getTotalBudget(),
                room.getAvailableBudget(),
                room.getTotalBudget() - room.getAvailableBudget(),
                room.getCurrency().name()
        );
    }
}
