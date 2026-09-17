package com.budzet.domain.budget.service;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.repository.BudgetChangeRepository;
import com.budzet.domain.room.entity.Currency;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.room.repository.RoomRepository;
import com.budzet.global.exception.BusinessException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BudgetChangeRepository budgetChangeRepository;

    @InjectMocks
    private BudgetService budgetService;


    @Test
    @DisplayName("예산조회")
    void getBudget_success(){

        //given
        Room room = mock(Room.class);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(room.getId()).thenReturn(1L);
        when(room.getName()).thenReturn("동아리");
        when(room.getTotalBudget()).thenReturn(1000L);
        when(room.getAvailableBudget()).thenReturn(800L);
        when(room.getCurrency()).thenReturn(Currency.KRW);

        //when
        BudgetResponse result = budgetService.getBudget(1L);

        //then
        assertNotNull(result);
        assertEquals(1L,result.roomId());
        assertEquals("동아리",result.roomName());
        assertEquals(1000,result.totalBudget());
        assertEquals(800,result.availableBudget());
        assertEquals(200,result.reserveBudget()); // 실제예산 - 가용예산 = 예약예산 테스트
        assertEquals("KRW",result.currency());
    }


    @Test
    @DisplayName("예산조회 - 모임이 존재하지 않을 때")
    void getBudget_roomNotFound(){

        //given
        when(roomRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudget(1L)
        );

        assertEquals("해당 모임이 존재하지 않습니다.",exception.getMessage());
    }

    @Test
    @DisplayName("예산조회 - 예산이 존재하지 않을 때")
    void getBudget_budgetNotFound(){

        //given
        Room room = mock(Room.class);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(room.getTotalBudget()).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudget(1L)
        );

        assertEquals("해당 모임에 등록된 예산 정보가 없습니다.",exception.getMessage());

    }

    @Test
    @DisplayName("예산 변경 내역 조회")
    void getBudgetHistory_success(){

        //given
        Room room = mock(Room.class);
        BudgetChange budgetChange = mock(BudgetChange.class);
        LocalDateTime now = LocalDateTime.now();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(budgetChangeRepository.findAllByRoomIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(budgetChange));

        when(budgetChange.getId()).thenReturn(10L);
        when(budgetChange.getChangedBudget()).thenReturn(8000L); //ChangedBudget 승인 금액이 아닌 실제 정산 금액 반환
        when(budgetChange.getType()).thenReturn(BudgetType.SETTLEMENT);
        when(budgetChange.getReason()).thenReturn("장비대여");
        when(budgetChange.getCreatedAt()).thenReturn(now);

        //when
        BudgetHistoryResponse result = budgetService.getBudgetHistory(1L);

        //then
        assertNotNull(result);
        assertEquals(1, result.history().size());

        BudgetHistoryResponse.HistoryItem historyItem = result.history().getFirst();
        assertEquals(10L,historyItem.id());
        assertEquals(8000L,historyItem.changedBudget());
        assertEquals("SETTLEMENT",historyItem.type());
        assertEquals("장비대여",historyItem.reason());
        assertEquals(now,historyItem.processedAt());
    }


    @Test
    @DisplayName("예산 변경 내역 조회 - 변동 내역이 없을 때 빈 리스트 반환")
    void getBudgetHistory_emptyHistory(){

        //given
        Room room = mock(Room.class);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(budgetChangeRepository.findAllByRoomIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        //when
        BudgetHistoryResponse result = budgetService.getBudgetHistory(1L);

        //then
        assertNotNull(result);
        assertTrue(result.history().isEmpty());
    }

    @Test
    @DisplayName("예산 변경 내역 조회 - 모임이 존재하지 않을 때")
    void getBudgetHistory_roomNotFound(){

        //given
        when(roomRepository.findById(1L))
                .thenReturn(Optional.empty());

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> budgetService.getBudgetHistory(1L)
        );

        assertEquals("해당 모임이 존재하지 않습니다.",exception.getMessage());
    }
}
