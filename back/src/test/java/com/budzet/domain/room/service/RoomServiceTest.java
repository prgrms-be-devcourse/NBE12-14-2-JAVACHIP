package com.budzet.domain.room.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budzet.domain.room.dto.RoomCreateRequest;
import com.budzet.domain.room.dto.RoomCreateResponse;
import com.budzet.domain.room.dto.RoomListResponse;
import com.budzet.domain.room.entity.Currency;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void createRoom_initializesAvailableBudgetWithTotalBudget() {
        RoomCreateRequest request = new RoomCreateRequest(
                "한양대 사진동아리 렌즈",
                100_000L,
                Currency.KRW
        );
        when(roomRepository.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoomCreateResponse response = roomService.createRoom(request);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());

        Room savedRoom = roomCaptor.getValue();
        assertEquals("한양대 사진동아리 렌즈", savedRoom.getName());
        assertEquals(100_000L, savedRoom.getTotalBudget());
        assertEquals(100_000L, savedRoom.getAvailableBudget());
        assertEquals(Currency.KRW, savedRoom.getCurrency());
        assertEquals(100_000L, response.availableBudget());
    }

    @Test
    void getRooms_returnsJoinedRoomList() {
        Long userId = 1L;
        Room firstRoom = Room.create("사진동아리", 100_000L, Currency.KRW);
        Room secondRoom = Room.create("해외여행", 500L, Currency.USD);
        when(roomRepository.findAllJoinedRoomsByUserId(userId))
                .thenReturn(List.of(firstRoom, secondRoom));

        RoomListResponse response = roomService.getRooms(userId);

        verify(roomRepository).findAllJoinedRoomsByUserId(userId);
        assertEquals(2, response.rooms().size());
        assertEquals("사진동아리", response.rooms().getFirst().name());
        assertEquals(100_000L, response.rooms().getFirst().availableBudget());
        assertEquals("USD", response.rooms().get(1).currency());
    }

    @Test
    void getRooms_returnsEmptyListWhenNoJoinedRoomsExist() {
        Long userId = 1L;
        when(roomRepository.findAllJoinedRoomsByUserId(userId))
                .thenReturn(List.of());

        RoomListResponse response = roomService.getRooms(userId);

        verify(roomRepository).findAllJoinedRoomsByUserId(userId);
        assertTrue(response.rooms().isEmpty());
    }
}
