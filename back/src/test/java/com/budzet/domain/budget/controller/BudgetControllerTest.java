package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.service.BudgetService;
import com.budzet.domain.room.entity.Currency;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.user.entity.User;
import com.budzet.global.rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "JWT_SECRET=test_jwt_secret_key_12345678901234567890")
@AutoConfigureMockMvc
public class BudgetControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private Rq rq;

    private final Long testUserId = 100L;

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 Rq가 호출될 때 공통으로 사용할 가짜 로그인 유저 세팅
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(testUserId);
        given(rq.getActor()).willReturn(mockUser);
    }

    @Test
    @DisplayName("예산조회 성공")
    void getBudget_success() throws Exception {

        // given
        Long roomId = 1L;
        Room room = Room.create("모임방", 1000L, Currency.KRW);
        ReflectionTestUtils.setField(room, "id", roomId);

        BudgetResponse budgetResponse = BudgetResponse.from(room);

        given(budgetService.getBudget(roomId, testUserId)).willReturn(budgetResponse);

        // when & then
        mvc.perform(get("/rooms/{roomId}/budget",roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.message").value("예산 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.roomId").value(1L))
                .andExpect(jsonPath("$.data.roomName").value("모임방"))
                .andExpect(jsonPath("$.data.totalBudget").value(1000L))
                .andExpect(jsonPath("$.data.availableBudget").value(1000L))
                .andExpect(jsonPath("$.data.reserveBudget").value(0))
                .andExpect(jsonPath("$.data.currency").value("KRW"));
    }

    @Test
    @DisplayName("예산 변경 내역 조회 성공")
    void getBudgetHistory_success() throws Exception {

        // given
        Long roomId = 1L;
        LocalDateTime now = LocalDateTime.now();
        BudgetChange budgetChange = mock(BudgetChange.class);

        given(budgetChange.getId()).willReturn(10L);
        given(budgetChange.getChangedBudget()).willReturn(8000L);
        given(budgetChange.getType()).willReturn(BudgetType.SETTLEMENT);
        given(budgetChange.getReason()).willReturn("장비대여");
        given(budgetChange.getCreatedAt()).willReturn(now);

        BudgetHistoryResponse response = BudgetHistoryResponse.from(List.of(budgetChange));

        given(budgetService.getBudgetHistory(roomId, testUserId)).willReturn(response);

        // when & then
        mvc.perform(get("/rooms/{roomId}/budget/history",roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.message").value("예산 변동 목록 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.history[0].id").value(10L))
                .andExpect(jsonPath("$.data.history[0].changedBudget").value(8000L))
                .andExpect(jsonPath("$.data.history[0].type").value("SETTLEMENT"))
                .andExpect(jsonPath("$.data.history[0].reason").value("장비대여"))
                .andExpect(jsonPath("$.data.history[0].reason").exists());
    }
}