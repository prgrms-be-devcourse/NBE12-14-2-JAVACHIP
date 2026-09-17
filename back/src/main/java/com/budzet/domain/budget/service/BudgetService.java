package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final RoomRepository roomRepository;
    private final BudgetChangeRepository budgetChangeRepository;
    private final UserRoomConnectionRepository userRoomConnectionRepository;

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long roomId, Long userId){

        validateRoomMember(roomId, userId);
        Room room = findByRoomId(roomId);
        if(room.getTotalBudget() == null || room.getAvailableBudget() == null){
            throw new BusinessException(ErrorCode.BUDGET_NOT_FOUND);
        }
        return BudgetResponse.from(room);
    }

    @Transactional(readOnly = true)
    public BudgetHistoryResponse getBudgetHistory(Long roomId, Long userId) {

        validateRoomMember(roomId, userId);
        findByRoomId(roomId);
        List<BudgetChange> budgetChanges = budgetChangeRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId);
        return BudgetHistoryResponse.from(budgetChanges);
    }



    private Room findByRoomId(Long roomId){

        return roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    private UserRoomConnection validateRoomMember(Long roomId, Long userId){

        return userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId,roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_JOINED_ROOM));
    }
}



