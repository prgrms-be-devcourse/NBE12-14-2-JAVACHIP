package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.dto.BudgetUpdateRequest;
import com.budzet.domain.budget.dto.BudgetUpdateResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.room.entity.Authority;
import com.budzet.domain.room.entity.Currency;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.domain.user.entity.User;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BudgetChangeRepository budgetChangeRepository;

    @Mock
    private UserRoomConnectionRepository userRoomConnectionRepository;

    @InjectMocks
    private BudgetService budgetService;


    @Test
    @DisplayName("예산조회")
    void getBudget_success() {

        //given
        Long roomId = 1L;
        Long userId = 100L;
        Room room = mock(Room.class);
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(room.getId()).thenReturn(1L);
        when(room.getName()).thenReturn("동아리");
        when(room.getTotalBudget()).thenReturn(1000L);
        when(room.getAvailableBudget()).thenReturn(800L);
        when(room.getCurrency()).thenReturn(Currency.KRW);

        //when
        BudgetResponse result = budgetService.getBudget(roomId, userId);

        //then
        assertNotNull(result);
        assertEquals(1L, result.roomId());
        assertEquals("동아리", result.roomName());
        assertEquals(1000L, result.totalBudget());
        assertEquals(800L, result.availableBudget());
        assertEquals(200L, result.reserveBudget());
        assertEquals("KRW", result.currency());
    }


    @Test
    @DisplayName("예산조회 - 모임이 존재하지 않을 때")
    void getBudget_roomNotFound() {

        //given
        Long roomId = 1L;
        Long userId = 100L;
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.empty());

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudget(roomId, userId)
        );

        assertEquals("해당 모임이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("예산조회 - 예산이 존재하지 않을 때")
    void getBudget_budgetNotFound() {

        //given
        Long roomId = 1L;
        Long userId = 100L;
        Room room = mock(Room.class);
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(room.getTotalBudget()).thenReturn(null);

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudget(roomId, userId)
        );

        assertEquals("해당 모임에 등록된 예산 정보가 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("예산조회 - 방의 멤버가 아닐 때")
    void getBudget_userNotJoinedRoom() {

        //given
        Long roomId = 1L;
        Long userId = 100L;

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.empty());

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudget(roomId, userId)
        );

        assertEquals("사용자가 방에 속해있지 않습니다.", exception.getMessage());

    }

    @Test
    @DisplayName("예산 변경 내역 조회")
    void getBudgetHistory_success() {

        //given
        Long roomId = 1L;
        Long userId = 100L;
        BudgetChange budgetChange = mock(BudgetChange.class);
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);
        Room room = mock(Room.class);
        LocalDateTime now = LocalDateTime.now();

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));

        when(budgetChangeRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId))
                .thenReturn(List.of(budgetChange));
        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(budgetChange.getId()).thenReturn(10L);
        when(budgetChange.getChangedBudget()).thenReturn(8000L);
        when(budgetChange.getType()).thenReturn(BudgetType.SETTLEMENT);
        when(budgetChange.getUserName()).thenReturn("홍길동");
        when(budgetChange.getReason()).thenReturn("장비대여");
        when(budgetChange.getCreatedAt()).thenReturn(now);

        //when
        BudgetHistoryResponse result = budgetService.getBudgetHistory(roomId, userId);

        //then
        assertNotNull(result);
        assertEquals(1, result.history().size());

        BudgetHistoryResponse.HistoryItem historyItem = result.history().getFirst();
        assertEquals(10L, historyItem.id());
        assertEquals(8000L, historyItem.changedBudget());
        assertEquals("SETTLEMENT", historyItem.type());
        assertEquals("홍길동", historyItem.userName());
        assertEquals("장비대여", historyItem.reason());
        assertEquals(now, historyItem.processedAt());
    }


    @Test
    @DisplayName("예산 변경 내역 조회 - 변동 내역이 없을 때 빈 리스트 반환")
    void getBudgetHistory_emptyHistory() {

        //given
        Long roomId = 1L;
        Long userId = 100L;
        Room room = mock(Room.class);
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));

        when(budgetChangeRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId))
                .thenReturn(Collections.emptyList());

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        //when
        BudgetHistoryResponse result = budgetService.getBudgetHistory(roomId, userId);

        //then
        assertNotNull(result);
        assertTrue(result.history().isEmpty());
    }

    @Test
    @DisplayName("예산 변경 내역 조회 - 방의 멤버가 아닐 때")
    void getBudgetHistory_userNotJoinedRoom() {

        //given
        Long roomId = 1L;
        Long userId = 100L;

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.empty());

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudgetHistory(roomId, userId)
        );

        assertEquals("사용자가 방에 속해있지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("예산 수정 성공 - OWNER 권한으로 증액")
    void updateBudget_success_owner() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        BudgetUpdateRequest request = new BudgetUpdateRequest(5000L, BudgetType.INCREASE, "지원금 추가");

        User user = mock(User.class);
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);
        Room room = mock(Room.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));
        when(userRoomConnection.getAuthority()).thenReturn(Authority.OWNER);
        when(userRoomConnection.getUser()).thenReturn(user);

        when(roomRepository.findByWithLock(roomId))
                .thenReturn(Optional.of(room));
        when(room.getId()).thenReturn(roomId);
        when(room.getTotalBudget()).thenReturn(15000L);

        // when
        BudgetUpdateResponse response = budgetService.updateBudget(roomId, userId, request);

        // then
        assertNotNull(response);
        assertEquals(roomId, response.roomId());
        assertEquals(15000L, response.totalBudget());

        // 예산 연산 메서드가 호출되었는지 및 이력이 DB에 저장되었는지 검증
        verify(room).updateTotalBudget(5000L, BudgetType.INCREASE);
        verify(budgetChangeRepository, times(1)).save(any(BudgetChange.class));
    }

    @Test
    @DisplayName("예산 수정 성공 - OPERATOR 권한으로 차감")
    void updateBudget_success_operator() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        BudgetUpdateRequest request = new BudgetUpdateRequest(2000L, BudgetType.DECREASE, "비품 구매");

        User user = mock(User.class);
        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);
        Room room = mock(Room.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));
        when(userRoomConnection.getAuthority()).thenReturn(Authority.OPERATOR);
        when(userRoomConnection.getUser()).thenReturn(user);

        when(roomRepository.findByWithLock(roomId))
                .thenReturn(Optional.of(room));
        when(room.getId()).thenReturn(roomId);
        when(room.getTotalBudget()).thenReturn(8000L);

        // when
        BudgetUpdateResponse response = budgetService.updateBudget(roomId, userId, request);

        // then
        assertNotNull(response);
        verify(room).updateTotalBudget(2000L, BudgetType.DECREASE);
        verify(budgetChangeRepository, times(1)).save(any(BudgetChange.class));
    }

    @Test
    @DisplayName("예산 수정 실패 - 권한 없음 (일반 MEMBER)")
    void updateBudget_forbiddenAccess() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        BudgetUpdateRequest request = new BudgetUpdateRequest(1000L, BudgetType.INCREASE, "시도");

        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));
        when(userRoomConnection.getAuthority()).thenReturn(Authority.MEMBER); // 일반 회원 권한

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.updateBudget(roomId, userId, request)
        );

        // 락 조회 및 저장 메서드가 실행되지 않아야 함
        verify(roomRepository, never()).findByWithLock(any());
        verify(budgetChangeRepository, never()).save(any());
    }

    @Test
    @DisplayName("예산 수정 실패 - 방을 찾을 수 없음")
    void updateBudget_roomNotFound() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        BudgetUpdateRequest request = new BudgetUpdateRequest(1000L, BudgetType.INCREASE, "시도");

        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));
        when(userRoomConnection.getAuthority()).thenReturn(Authority.OWNER);

        when(roomRepository.findByWithLock(roomId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> budgetService.updateBudget(roomId, userId, request));
        verify(budgetChangeRepository, never()).save(any());
    }

    @Test
    @DisplayName("예산 수정 실패 - 가용예산 부족으로 차감 예외 발생 시 이력이 저장되지 않음")
    void updateBudget_exceedAvailableBudget() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        BudgetUpdateRequest request = new BudgetUpdateRequest(50000L, BudgetType.DECREASE, "과도한 차감");

        UserRoomConnection userRoomConnection = mock(UserRoomConnection.class);
        Room room = mock(Room.class);

        when(userRoomConnectionRepository.findByUser_IdAndRoom_Id(userId, roomId))
                .thenReturn(Optional.of(userRoomConnection));
        when(userRoomConnection.getAuthority()).thenReturn(Authority.OWNER);

        when(roomRepository.findByWithLock(roomId))
                .thenReturn(Optional.of(room));

        // Room 엔티티 예산 차감 시 예외 발생하도록 처리
        doThrow(new BusinessException(ErrorCode.BAD_REQUEST, "가용예산이 부족합니다."))
                .when(room).updateTotalBudget(50000L, BudgetType.DECREASE);

        // when & then
        assertThrows(BusinessException.class, () -> budgetService.updateBudget(roomId, userId, request));

        // 예외가 발생했으므로 이력 저장은 진행되지 않음
        verify(budgetChangeRepository, never()).save(any());
    }
}