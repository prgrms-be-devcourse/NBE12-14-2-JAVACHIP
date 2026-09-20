package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetChangeCreateRequest;
import com.budzet.domain.budget.dto.BudgetChangeCreateResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Authority;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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
                createRequest.reason(),
                budgetRequest.getRequestedAmount(),
                createRequest.changedBudget()
        );
        budgetChange.addBudgetRequest(budgetRequest);

        //todo 상태 타입 확인 후 변경 (String, enum)
        //신청 상태 변경 승인 -> 정산
        budgetRequest.changeToSettlement("SETTLEMENT");

        //실제 예산 변경처리
        room.settleBudget(budgetChange.getChangeBudget(), budgetChange.getChangedBudget());

        //정산 후 저장
        room.addBudgetChange(budgetChange);
        budgetChangeRepository.save(budgetChange);

        return BudgetChangeCreateResponse.from(budgetChange);
    }

    @Transactional(readOnly = true)
    public BudgetChangeListResponse budgetChangeList(Long roomId, Long userId){

        budgetService.validateRoomMember(roomId,userId);

        List<BudgetChange> budgetChanges =
                budgetChangeRepository.findAllByRoomIdAndTypeOrderByCreatedAtDesc(roomId, BudgetType.SETTLEMENT);

        return BudgetChangeListResponse.from(budgetChanges);
    }

    @Transactional(readOnly = true)
    public BudgetChangeDetailResponse budgetChangeDetail(Long roomId, Long userId, Long changeId){

        budgetService.validateRoomMember(roomId, userId);

        BudgetChange budgetChange = budgetChangeRepository.findById(changeId)
                .orElseThrow(()-> new BusinessException(ErrorCode.BUDGET_CHANGE_NOT_FOUND));

        return BudgetChangeDetailResponse.from(budgetChange);
    }

    @Transactional
    public BudgetChangeUpdateResponse UpdateBudgetChange(
            Long roomId, Long userId, Long changeId, BudgetChangeUpdateRequest updateRequest){

        UserRoomConnection connection = budgetService.validateRoomMember(roomId, userId);
        if(!(connection.getAuthority() == Authority.OWNER || connection.getAuthority() == Authority.OPERATOR)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        //입력 받은 유저이름과 이메일로 유저를 찾아 가져옴
        User user = userRepository.findByNameAndEmail(updateRequest.userName(),updateRequest.userEmail())
                .orElseThrow(()-> new BusinessException(ErrorCode.BAD_REQUEST,"유저 이름과 이메일이 일치하지 않습니다."));

        BudgetChange budgetChange = budgetChangeRepository.findById(changeId)
                .orElseThrow(()-> new BusinessException(ErrorCode.BUDGET_CHANGE_NOT_FOUND));

        //todo 정산 금액도 수정 가능시 사용 및 예외처리 추가, 아니면 삭제 및 변경 메서드 수정

        //방 예산도 수정
        /* 수정 전 금액 - 수정 후 금액 계산 그다음 현재 실제, 가용예산예 더하기 처리
            기존 실제 예산 2000 일때
            실제 정산 금액이 1000(수정전) - 800(수정후) = 200  -> 실제예산 + 200
            실제 정산 금액이 1000(수정전) - 1200(수정후) = -200 -> 실제예산 +(-200) */
        Long resultChanged = budgetChange.getChangedBudget() - updateRequest.changedBudget();
        Room room = budgetService.findByRoomIdWithLock(roomId);
        room.updateTotalBudget(resultChanged);


        //정산 내역 수정
        budgetChange.updateChange(updateRequest,user);

        //신청 내역이 존재하면 같이 수정
        if(budgetChange.getRequest() != null){
            budgetChange.getRequest().updateRequest(budgetChange,user);
        }

        return BudgetChangeUpdateResponse.from(budgetChange);
    }
}
