package com.budzet.domain.budget.service;

import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnectionId;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetRequestServiceTest {

    @Mock
    private BudgetRequestRepository budgetRequestRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRoomConnectionRepository userRoomConnectionRepository;

    @InjectMocks
    private BudgetRequestService budgetRequestService;

    @Test
    @DisplayName("성공: 유저가 방에 속해있고 가용 예산이 충분하면 예산 신청을 정상 등록(저장)한다.")
    void budgetRequestRegistration_Success() {
        // given
        Long roomId = 10L;
        Long userId = 1L;
        String reason = "동아리 비품 구매";
        Long requestedAmount = 50000L;

        // 1. 방 소속 여부 조회 결과 -> true 리턴 설정
        UserRoomConnectionId connectionId = new UserRoomConnectionId(userId, roomId);
        when(userRoomConnectionRepository.existsById(connectionId)).thenReturn(true);

        // 2. 가용 예산이 충분한 가짜 방(Room) 객체 생성 및 리턴 설정
        Room mockRoom = mock(Room.class);
        when(mockRoom.getAvailableBudget()).thenReturn(100000L); // 10만 원 (신청액 5만 원보다 큼)
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        // when
        budgetRequestService.budgetRequestRegistration(roomId, userId, reason, requestedAmount);

        // then: 최종적으로 데이터베이스에 save()가 정확히 1번 호출되었는지 검증 (void 메서드 검증)
        verify(budgetRequestRepository, times(1)).save(any(BudgetRequest.class));
    }

    @Test
    @DisplayName("실패: 유저가 해당 방에 가입되어 있지 않으면 BusinessException이 발생한다.")
    void budgetRequestRegistration_Fail_UserNotJoined() {
        // given
        Long roomId = 10L;
        Long userId = 1L;
        UserRoomConnectionId connectionId = new UserRoomConnectionId(userId, roomId);

        // 방에 소속되어 있지 않음 (false)
        when(userRoomConnectionRepository.existsById(connectionId)).thenReturn(false);

        // when & then: 예외가 발생하는지 검증
        assertThatThrownBy(() -> budgetRequestService.budgetRequestRegistration(roomId, userId, "이유", 5000L))
                .isInstanceOf(BusinessException.class);
        // .hasMessageContaining(ErrorCode.USER_NOT_JOINED_ROOM.getMessage()); // 필요시 추가

        // 에러가 났으므로 뒷단 로직(save)은 절대 실행되면 안 됨
        verify(budgetRequestRepository, never()).save(any(BudgetRequest.class));
    }

    @Test
    @DisplayName("실패: 신청 예산이 방의 가용 예산을 초과하면 BusinessException이 발생한다.")
    void budgetRequestRegistration_Fail_OverBudget() {
        // given
        Long roomId = 10L;
        Long userId = 1L;
        Long requestedAmount = 200000L; // 신청액 20만 원

        // 방 소속 여부는 통과
        UserRoomConnectionId connectionId = new UserRoomConnectionId(userId, roomId);
        when(userRoomConnectionRepository.existsById(connectionId)).thenReturn(true);

        // 방의 가용 예산은 10만 원으로 설정 (신청액이 초과됨)
        Room mockRoom = mock(Room.class);
        when(mockRoom.getAvailableBudget()).thenReturn(100000L);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        // when & then
        assertThatThrownBy(() -> budgetRequestService.budgetRequestRegistration(roomId, userId, "이유", requestedAmount))
                .isInstanceOf(BusinessException.class);

        // 에러가 났으므로 save는 절대 실행되면 안 됨
        verify(budgetRequestRepository, never()).save(any(BudgetRequest.class));
    }
}
