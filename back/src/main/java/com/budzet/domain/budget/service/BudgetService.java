package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.repository.RoomRepository;
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

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long roomId){
       Room room = this.findByRoomId(roomId);

        if(room.getTotalBudget() == null || room.getAvailableBudget() == null){
            throw new BusinessException(ErrorCode.BUDGET_NOT_FOUND);
        }

        return BudgetResponse.from(room);
    }

    @Transactional(readOnly = true)
    public BudgetHistoryResponse getBudgetHistory(Long roomId) {

        findByRoomId(roomId); //단순 방 존재 여부 확인용

        List<BudgetChange> budgetChanges = budgetChangeRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId);

        return BudgetHistoryResponse.from(budgetChanges);
    }



    private Room findByRoomId(Long roomId){

        //Room 조회 및 Room존재여부 확인 -> 중복 제거하기 위해 메서드 분리

        return this.roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }
}



