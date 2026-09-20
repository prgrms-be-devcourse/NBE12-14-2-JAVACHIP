package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetChangeCreateRequest;
import com.budzet.domain.budget.dto.BudgetChangeCreateResponse;
import com.budzet.domain.budget.dto.BudgetChangeListResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.budget.repository.BudgetRequestRepository;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.user.entity.User;
import com.budzet.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetChangeServiceTest {

    @Mock
    private BudgetChangeRepository budgetChangeRepository;

    @Mock
    private BudgetRequestRepository budgetRequestRepository;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetChangeService budgetChangeService;

    @Test
    @DisplayName("정산 등록 성공 - 정상적으로 예산이 차감되고 변경 이력이 저장됨")
    void createBudgetChange_success() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        Long requestId = 10L;

        BudgetChangeCreateRequest request = new BudgetChangeCreateRequest(8000L, "실제 장비 구매 영수증 첨부");

        User user = mock(User.class);
        UserRoomConnection connection = mock(UserRoomConnection.class);
        BudgetRequest budgetRequest = mock(BudgetRequest.class);
        Room room = mock(Room.class);

        when(budgetService.validateRoomMember(roomId, userId)).thenReturn(connection);
        when(connection.getUser()).thenReturn(user);

        when(budgetRequestRepository.findById(requestId)).thenReturn(Optional.of(budgetRequest));
        when(budgetRequest.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(budgetRequest.getStatus()).thenReturn("APPROVED");
        when(budgetRequest.getReason()).thenReturn("동아리 장비 구매 신청");
        when(budgetRequest.getRequestedAmount()).thenReturn(10000L);

        when(budgetService.findByRoomIdWithLock(roomId)).thenReturn(room);

        when(budgetChangeRepository.save(any(BudgetChange.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        BudgetChangeCreateResponse response = budgetChangeService.createBudgetChange(roomId, userId, requestId, request);

        // then
        assertNotNull(response);
        assertEquals(10000L, response.changeBudget());
        assertEquals(8000L, response.changedBudget());
        assertEquals("실제 장비 구매 영수증 첨부", response.changeReason());
        assertEquals(BudgetType.SETTLEMENT, response.budgetType());

        verify(room, times(1)).settleBudget(10000L, 8000L);
        verify(budgetChangeRepository, times(1)).save(any(BudgetChange.class));
    }

    @Test
    @DisplayName("정산 등록 실패 - 신청서가 존재하지 않을 때")
    void createBudgetChange_requestNotFound() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        Long requestId = 10L;
        BudgetChangeCreateRequest request = new BudgetChangeCreateRequest(8000L, "정산 사유");

        UserRoomConnection connection = mock(UserRoomConnection.class);

        when(budgetService.validateRoomMember(roomId, userId)).thenReturn(connection);
        when(budgetRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(
                BusinessException.class,
                () -> budgetChangeService.createBudgetChange(roomId, userId, requestId, request)
        );

        verify(budgetService, never()).findByRoomIdWithLock(any());
        verify(budgetChangeRepository, never()).save(any());
    }

    @Test
    @DisplayName("정산 등록 실패 - 신청서의 작성자가 아닐 때")
    void createBudgetChange_forbiddenRequestWriter() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        Long otherUserId = 200L;
        Long requestId = 10L;

        BudgetChangeCreateRequest request = new BudgetChangeCreateRequest(8000L, "정산 사유");

        UserRoomConnection connection = mock(UserRoomConnection.class);
        BudgetRequest budgetRequest = mock(BudgetRequest.class);
        User writerUser = mock(User.class);

        when(budgetService.validateRoomMember(roomId, userId)).thenReturn(connection);
        when(budgetRequestRepository.findById(requestId)).thenReturn(Optional.of(budgetRequest));

        when(budgetRequest.getUser()).thenReturn(writerUser);
        when(writerUser.getId()).thenReturn(otherUserId);

        // when & then
        assertThrows(
                BusinessException.class,
                () -> budgetChangeService.createBudgetChange(roomId, userId, requestId, request)
        );

        verify(budgetChangeRepository, never()).save(any());
    }

    @Test
    @DisplayName("정산 등록 실패 - 승인(APPROVED)된 요청이 아닐 때")
    void createBudgetChange_notApproved() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        Long requestId = 10L;

        BudgetChangeCreateRequest request = new BudgetChangeCreateRequest(8000L, "정산 사유");

        User user = mock(User.class);
        UserRoomConnection connection = mock(UserRoomConnection.class);
        BudgetRequest budgetRequest = mock(BudgetRequest.class);

        when(budgetService.validateRoomMember(roomId, userId)).thenReturn(connection);
        when(budgetRequestRepository.findById(requestId)).thenReturn(Optional.of(budgetRequest));

        when(budgetRequest.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(budgetRequest.getStatus()).thenReturn("PENDING");

        // when & then
        assertThrows(
                BusinessException.class,
                () -> budgetChangeService.createBudgetChange(roomId, userId, requestId, request)
        );

        verify(budgetChangeRepository, never()).save(any());
    }

    @Test
    @DisplayName("정산 등록 실패 - 방 멤버가 아닐 때")
    void createBudgetChange_userNotJoinedRoom() {
        // given
        Long roomId = 1L;
        Long userId = 100L;
        Long requestId = 10L;
        BudgetChangeCreateRequest request = new BudgetChangeCreateRequest(8000L, "정산 사유");

        when(budgetService.validateRoomMember(roomId, userId))
                .thenThrow(new BusinessException(null, "사용자가 방에 속해있지 않습니다."));

        // when & then
        assertThrows(
                BusinessException.class,
                () -> budgetChangeService.createBudgetChange(roomId, userId, requestId, request)
        );

        verify(budgetRequestRepository, never()).findById(any());
        verify(budgetChangeRepository, never()).save(any());
    }

    @Test
    @DisplayName("예산 변경 내역 목록 조회 성공 - 본인의 변경 내역만 최신순으로 반환")
    void budgetChangeList_success_onlyOwnChanges() {
        // given
        Long roomId = 1L;
        Long userId = 100L;

        UserRoomConnection connection = mock(UserRoomConnection.class);
        BudgetChange change1 = mock(BudgetChange.class);
        BudgetChange change2 = mock(BudgetChange.class);
        List<BudgetChange> ownBudgetChanges = List.of(change1, change2);

        // budgetChangeList 내부 모킹
        when(budgetService.validateRoomMember(roomId, userId)).thenReturn(connection);
        when(budgetChangeRepository.findAllByRoomIdAndUserIdOrderByCreatedAtDesc(roomId, userId))
                .thenReturn(ownBudgetChanges);

        // when
        BudgetChangeListResponse response = budgetChangeService.budgetChangeList(roomId, userId);

        // then
        assertNotNull(response);

        // 검증 1: 방 멤버 검증 호출
        verify(budgetService, times(1)).validateRoomMember(roomId, userId);

        // 검증 2: 정확히 파라미터로 전달된 본인의 userId 조건으로만 조회가 발생했는지 확인
        verify(budgetChangeRepository, times(1))
                .findAllByRoomIdAndUserIdOrderByCreatedAtDesc(eq(roomId), eq(userId));
    }

    @Test
    @DisplayName("예산 변경 내역 목록 조회 실패 - 방 멤버가 아닐 때")
    void budgetChangeList_userNotJoinedRoom() {
        // given
        Long roomId = 1L;
        Long userId = 100L;

        // 멤버 검증 시 예외 발생 모킹
        when(budgetService.validateRoomMember(roomId, userId))
                .thenThrow(new BusinessException(null, "사용자가 방에 속해있지 않습니다."));

        // when & then
        assertThrows(
                BusinessException.class,
                () -> budgetChangeService.budgetChangeList(roomId, userId)
        );

        // 검증: 멤버 검증 실패 시 DB 조회가 실행되지 않아야 함
        verify(budgetChangeRepository, never())
                .findAllByRoomIdAndUserIdOrderByCreatedAtDesc(anyLong(), anyLong());
    }

    @Test
    @DisplayName("예산 변경 내역 목록 조회 성공 - 본인의 변경 내역이 없는 경우 빈 리스트 반환")
    void budgetChangeList_emptyList() {
        // given
        Long roomId = 1L;
        Long userId = 100L;

        UserRoomConnection connection = mock(UserRoomConnection.class);

        when(budgetService.validateRoomMember(roomId, userId)).thenReturn(connection);
        when(budgetChangeRepository.findAllByRoomIdAndUserIdOrderByCreatedAtDesc(roomId, userId))
                .thenReturn(Collections.emptyList());

        // when
        BudgetChangeListResponse response = budgetChangeService.budgetChangeList(roomId, userId);

        // then
        assertNotNull(response);

        verify(budgetService, times(1)).validateRoomMember(roomId, userId);
        verify(budgetChangeRepository, times(1))
                .findAllByRoomIdAndUserIdOrderByCreatedAtDesc(eq(roomId), eq(userId));
    }
}