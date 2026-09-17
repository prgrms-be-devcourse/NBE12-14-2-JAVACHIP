package com.budzet.domain.room.repository;

import com.budzet.domain.room.entity.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
            SELECT room
            FROM Room room
            JOIN room.userConnections connection
            WHERE connection.user.id = :userId
              AND connection.joined = true
            ORDER BY room.createdAt DESC
            """)
    List<Room> findAllJoinedRoomsByUserId(@Param("userId") Long userId);
}
