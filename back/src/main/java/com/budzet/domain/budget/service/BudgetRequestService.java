package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetRequestListResponse;
import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnectionId;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.domain.user.entity.User;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetRequestService {

    private final BudgetRequestRepository budgetRequestRepository;
    private final RoomRepository roomRepository;
    private final UserRoomConnectionRepository userRoomConnectionRepository;

    /**
     * 유저가 전송된 roomId의 방소속인지 확인
     * @param userRoomConnectionId
     * @throws BusinessException USER_NOT_JOINED_ROOM
     */
    private void userInRoomCheck(UserRoomConnectionId userRoomConnectionId){
        if(!userRoomConnectionRepository.existsById(userRoomConnectionId))
            throw new BusinessException(ErrorCode.USER_NOT_JOINED_ROOM);
    }

    /**
     * 예산신청 등록
     * @param roomId
     * @param user
     * @param reason
     * @param requestedAmount
     */
    public void budgetRequestRegistration(Long roomId, User user, String reason, Long requestedAmount){

        userInRoomCheck(new UserRoomConnectionId(user.getId(), roomId));

        Room room = this.roomRepository.findById(roomId).get();
        //신청예산의 가용예산 초과여부 처리
        if(room.getAvailableBudget() < requestedAmount)
            throw new BusinessException(ErrorCode.REQUEST_AMOUNT_OVER_BUDGET);

        budgetRequestRepository.save(new BudgetRequest(room, user, reason, requestedAmount));
    }

    /**
     * 현재방의 예산신청 목록조회
     * @param roomId
     * @param user
     * @return 현재방의 예산신청 목록
     */
    public List<BudgetRequestListResponse> getBudgetRequestList(Long roomId, User user){

        userInRoomCheck(new UserRoomConnectionId(user.getId(), roomId));

        return budgetRequestRepository.findDtoByRoomId(roomId);
    }

    /**
     * 예산신청 상세조회
     * @param roomId
     * @param user
     * @param requestId
     * @return requestId의 예산신청 상세정보
     */
    public BudgetRequestListResponse getBudgetRequest(Long roomId, User user, Long requestId){

        userInRoomCheck(new UserRoomConnectionId(user.getId(), roomId));

        return budgetRequestRepository.findDtoById(roomId, requestId);
    }

}
