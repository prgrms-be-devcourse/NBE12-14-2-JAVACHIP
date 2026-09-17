package com.budzet.domain.room.service;

import com.budzet.domain.room.dto.RoomCreateRequest;
import com.budzet.domain.room.dto.RoomCreateResponse;
import com.budzet.domain.room.dto.RoomDetailResponse;
import com.budzet.domain.room.dto.RoomListResponse;
import com.budzet.domain.room.dto.RoomUpdateRequest;
import com.budzet.domain.room.entity.Authority;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRoomConnectionRepository userRoomConnectionRepository;

    // TODO: (김영우)
    // TODO: 인증 연동 후 Long userId를 파라미터로 받기
    // TODO: Room과 UserRoomConnection 저장을 하나의 트랜잭션으로 묶을 예정
    @Transactional
    public RoomCreateResponse createRoom(RoomCreateRequest request) {
        Room room = Room.create(
                request.name(),
                request.totalBudget(),
                request.currency()
        );

        // TODO: User 조회하기(영속성 객체 필요)

        Room savedRoom = roomRepository.save(room);
        // TODO: Room 저장 후 UserRoomConnection을 OWNER, joined=true로 함께 저장하기

        return RoomCreateResponse.from(savedRoom);
    }

    // TODO: (김영우)
    // TODO: 페이징 처리 추가할지 논의한 후, 결정되면 추가 구현하기
    @Transactional(readOnly = true)
    public RoomListResponse getRooms(Long userId) {
        List<Room> rooms = roomRepository.findAllJoinedRoomsByUserId(userId);
        return RoomListResponse.from(rooms);
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoom(Long userId, Long roomId) {
        Room room = roomRepository.findJoinedRoomByIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ROOM_NOT_FOUND
                ));

        return RoomDetailResponse.from(room);
    }

    @Transactional
    public RoomDetailResponse updateRoom(
            Long userId,
            Long roomId,
            RoomUpdateRequest request
    ) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ROOM_NOT_FOUND
                ));

        UserRoomConnection connection = userRoomConnectionRepository
                .findByUser_IdAndRoom_Id(userId, roomId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.MEMBER_NOT_FOUND
                ));

        if (connection.getAuthority() != Authority.OWNER) {
            throw new BusinessException(ErrorCode.OWNER_REQUIRED);
        }

        room.changeName(request.name());
        return RoomDetailResponse.from(room);
    }
}
