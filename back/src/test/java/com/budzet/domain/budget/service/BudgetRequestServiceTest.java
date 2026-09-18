package com.budzet.domain.budget.service;

import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnectionId;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.domain.user.entity.User;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 환경 구축
class BudgetRequestServiceTest {

    @Mock
    private BudgetRequestRepository budgetRequestRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRoomConnectionRepository userRoomConnectionRepository;

    @InjectMocks
    private BudgetRequestService budgetRequestService;

    private User user;
    private Room room;
    private Long roomId;
    private String reason;
    private Long requestedAmount;

    @BeforeEach
    void setUp() {
        roomId = 1L;
        reason = "비품 구매";
        requestedAmount = 50000L;

        // 테스트용 가짜 객체 생성 및 스터빙 설정용 ID 할당
        user = mock(User.class);
        when(user.getId()).thenReturn(10L);

        room = mock(Room.class);
    }

    @Test
    @DisplayName("성공: 유저가 방에 속해있고 신청 금액이 가용 예산 이내이면 예산 신청이 정상 등록된다.")
    void budgetRequestRegistration_Success() {
        // given
        UserRoomConnectionId connectionId = new UserRoomConnectionId(user.getId(), roomId);
        when(userRoomConnectionRepository.existsById(connectionId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getAvailableBudget()).thenReturn(100000L); // 가용 예산 100,000원 (신청 금액 50,000원보다 많음)

        // when & then
        assertDoesNotThrow(() ->
                budgetRequestService.budgetRequestRegistration(roomId, user, reason, requestedAmount)
        );

        // 실제로 데이터베이스 저장 로직이 호출되었는지 검증
        verify(budgetRequestRepository, times(1)).save(any(BudgetRequest.class));
    }

    @Test
    @DisplayName("실패: 유저가 해당 방에 소속되어 있지 않으면 USER_NOT_JOINED_ROOM 예외가 발생한다.")
    void budgetRequestRegistration_Fail_UserNotJoined() {
        // given
        UserRoomConnectionId connectionId = new UserRoomConnectionId(user.getId(), roomId);
        when(userRoomConnectionRepository.existsById(connectionId)).thenReturn(false); // 방에 소속되지 않음

        // when & then
        assertThatThrownBy(() ->
                budgetRequestService.budgetRequestRegistration(roomId, user, reason, requestedAmount)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_JOINED_ROOM.getMessage()); // 혹은 ErrorCode 비교로 변경 가능

        // 예외가 터졌으므로 이후 로직(조회 및 저장)은 실행되지 않아야 함
        verify(roomRepository, never()).findById(anyLong());
        verify(budgetRequestRepository, never()).save(any(BudgetRequest.class));
    }

    @Test
    @DisplayName("실패: 신청 금액이 방의 가용 예산을 초과하면 REQUEST_AMOUNT_OVER_BUDGET 예외가 발생한다.")
    void budgetRequestRegistration_Fail_AmountOverBudget() {
        // given
        UserRoomConnectionId connectionId = new UserRoomConnectionId(user.getId(), roomId);
        when(userRoomConnectionRepository.existsById(connectionId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getAvailableBudget()).thenReturn(30000L); // 가용 예산 30,000원 (신청 금액 50,000원보다 적음)

        // when & then
        assertThatThrownBy(() ->
                budgetRequestService.budgetRequestRegistration(roomId, user, reason, requestedAmount)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.REQUEST_AMOUNT_OVER_BUDGET.getMessage());

        // 예외가 터졌으므로 저장 로직은 호출되지 않아야 함
        verify(budgetRequestRepository, never()).save(any(BudgetRequest.class));
    }
}
