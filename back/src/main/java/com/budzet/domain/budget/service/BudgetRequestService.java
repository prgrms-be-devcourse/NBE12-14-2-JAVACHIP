package com.budzet.domain.budget.service;

import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnectionId;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudgetRequestService {

    private final BudgetRequestRepository budgetRequestRepository;
    private final RoomRepository roomRepository;
    private final UserRoomConnectionRepository userRoomConnectionRepository;

    public void budgetRequestRegistration(Long roomId, Long userId, String reason, Long requestedAmount){
        //유저 방 소속여부 처리
        boolean isUserJoinRoom = userRoomConnectionRepository.existsById(new UserRoomConnectionId(userId, roomId));
        if(!isUserJoinRoom)
            throw new BusinessException(ErrorCode.USER_NOT_JOINED_ROOM);

        Room room = this.roomRepository.findById(roomId).get();
        //신청예산의 가용예산 초과여부 처리
        if(room.getAvailableBudget() < requestedAmount)
            throw new BusinessException(ErrorCode.USER_NOT_JOINED_ROOM);

        budgetRequestRepository.save(new BudgetRequest(room, reason, requestedAmount));
    }

}
