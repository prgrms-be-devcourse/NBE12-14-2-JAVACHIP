package com.budzet.domain.room.dto;

import com.budzet.domain.room.entity.Room;
import java.time.LocalDateTime;
import java.util.List;

public record RoomListResponse(
        List<RoomItem> rooms
) {

    public static RoomListResponse from(List<Room> rooms) {
        List<RoomItem> roomItems = rooms.stream()
                .map(RoomItem::from)
                .toList();

        return new RoomListResponse(roomItems);
    }

    public record RoomItem(
            Long id,
            String name,
            Long totalBudget,
            Long availableBudget,
            String currency,
            LocalDateTime createdAt
    ) {

        private static RoomItem from(Room room) {
            return new RoomItem(
                    room.getId(),
                    room.getName(),
                    room.getTotalBudget(),
                    room.getAvailableBudget(),
                    room.getCurrency().name(),
                    room.getCreatedAt()
            );
        }
    }
}
