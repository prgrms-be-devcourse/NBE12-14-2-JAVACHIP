package com.budzet.domain.room.repository;

import com.budzet.domain.room.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
            SELECT room
            FROM Room room
            JOIN room.userConnections connection
            WHERE connection.user.id = :userId
            ORDER BY room.createdAt DESC
            """)
    List<Room> findAllJoinedRoomsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT room
            FROM Room room
            JOIN room.userConnections connection
            WHERE room.id = :roomId
              AND connection.user.id = :userId
            """)
    Optional<Room> findJoinedRoomByIdAndUserId(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT room
            FROM Room room
            WHERE room.id = :roomId
            """)
    Optional<Room> findByWithLock(@Param("roomId") Long roomId);
}
