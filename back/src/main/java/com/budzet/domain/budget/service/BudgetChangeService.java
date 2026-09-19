package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetChangeCreateRequest;
import com.budzet.domain.budget.dto.BudgetChangeCreateResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;

import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetChangeService {

    private final BudgetChangeRepository budgetChangeRepository;
    private final BudgetRequestRepository budgetRequestRepository;
    private final BudgetService budgetService;

    @Transactional
    public BudgetChangeCreateResponse createBudgetChange(
            Long roomId,
            Long userId,
            Long requestId,
            BudgetChangeCreateRequest createRequest){

        UserRoomConnection connection = budgetService.validateRoomMember(roomId, userId);
        BudgetRequest budgetRequest = budgetRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUDGET_REQUEST_NOT_FOUND));

        //해당 신청의 신청자인지 확인
        if(!budgetRequest.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.FORBIDDEN_REQUEST_WRITER);
        }

        //승인된 요청이 아닐 때 예외처리
        if(!"APPROVED".equals(budgetRequest.getStatus())){
            throw new BusinessException(ErrorCode.BUDGET_REQUEST_NOT_APPROVED);
        }

        Room room = budgetService.findByRoomIdWithLock(roomId);

        //변경 내역 생성
        BudgetType type = BudgetType.SETTLEMENT;

        BudgetChange budgetChange = new BudgetChange(
                room,
                connection.getUser(),
                connection.getUser().getName(),
                type,
                budgetRequest.getReason(),
                createRequest.changeReason(),
                budgetRequest.getRequestedAmount(),
                createRequest.changedBudget()
        );
        budgetChange.addBudgetRequest(budgetRequest);

        //실제 예산 변경처리
        room.settleBudget(budgetChange.getChangeBudget(), budgetChange.getChangedBudget());

        //정산 후 저장
        budgetChangeRepository.save(budgetChange);

        return BudgetChangeCreateResponse.from(budgetChange);
    }
}
