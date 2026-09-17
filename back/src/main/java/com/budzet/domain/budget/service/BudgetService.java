package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long roomId){
        Room room =  this.roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "해당 모임이 존재하지 않습니다."));

        if(room.getTotalBudget() == null || room.getAvailableBudget() == null){
            throw new BusinessException(ErrorCode.NOT_FOUND,
                    "해당 모임에 등록된 예산 정보가 없습니다.");
        }

        return BudgetResponse.from(room);
    }

}



