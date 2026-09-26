package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.dto.BudgetUpdateRequest;
import com.budzet.domain.budget.dto.BudgetUpdateResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.room.entity.Authority;
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
        List<BudgetType> budGetTypes = List.of(BudgetType.INCREASE,BudgetType.DECREASE);
        List<BudgetChange> budgetChanges = budgetChangeRepository.findAllByRoomIdAndTypeInOrderByCreatedAtDesc(
                roomId,budGetTypes
        );
        return BudgetHistoryResponse.from(budgetChanges);
    }

    @Transactional
    public BudgetUpdateResponse updateBudget(
            Long roomId, Long userId,
            BudgetUpdateRequest budgetUpdateRequest){

        UserRoomConnection userRoomConnection = validateRoomMember(roomId,userId);
        if(!(userRoomConnection.getAuthority().equals(Authority.OWNER)
        || userRoomConnection.getAuthority().equals(Authority.OPERATOR))){
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        Room room = findByRoomIdWithLock(roomId);

        room.updateTotalBudget(budgetUpdateRequest.totalBudget(),
                budgetUpdateRequest.budgetType());
        BudgetChange budgetChange = new BudgetChange(
                room,
                userRoomConnection.getUser(),
                userRoomConnection.getUser().getName(),
                budgetUpdateRequest.budgetType(),
                budgetUpdateRequest.reason(),
                budgetUpdateRequest.totalBudget()
                );
        room.addBudgetChange(budgetChange);
        budgetChangeRepository.save(budgetChange);

        return BudgetUpdateResponse.from(room);
    }


    public Room findByRoomId(Long roomId){
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    public Room findByRoomIdWithLock(Long roomId){

        return roomRepository.findByWithLock(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    public UserRoomConnection validateRoomMember(Long roomId, Long userId){

        return userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId,roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_JOINED_ROOM));
    }
}



