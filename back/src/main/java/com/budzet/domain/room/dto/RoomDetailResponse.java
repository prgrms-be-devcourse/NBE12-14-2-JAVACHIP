package com.budzet.domain.room.dto;

import com.budzet.domain.room.entity.Room;
import java.time.LocalDateTime;

public record RoomDetailResponse(
        Long id,
        String name,
        Long totalBudget,
        Long availableBudget,
        String currency,
        LocalDateTime createdAt
) {

    public static RoomDetailResponse from(Room room) {
        return new RoomDetailResponse(
                room.getId(),
                room.getName(),
                room.getTotalBudget(),
                room.getAvailableBudget(),
                room.getCurrency().name(),
                room.getCreatedAt()
        );
    }
}
