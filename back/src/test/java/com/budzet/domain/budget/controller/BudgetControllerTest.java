package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.service.BudgetService;
import com.budzet.domain.room.entity.Currency;
import com.budzet.domain.room.entity.Room;
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
@AutoConfigureMockMvc //MockMvc 사용 -> 실제 서버를 뛰우지 않고도 http테스트 가능하게
public class BudgetControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BudgetService budgetService;

    @Test
    @DisplayName("예산조회")
    void getBudget_success() throws Exception{

        //given
        Long roomId = 1L;

        Room room = Room.create("모임방", 1000L, Currency.KRW);

        // ReflectionTestUtils로 ID 강제 주입
        ReflectionTestUtils.setField(room,"id",1L);

        BudgetResponse budgetResponse = BudgetResponse.from(room);

        given(budgetService.getBudget(roomId)).willReturn(budgetResponse);

        //when & then
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
    @DisplayName("예산 변경 내역 조회")
    void getBudgetHistory_success() throws  Exception{

        //given
        Long roomId = 1L;
        LocalDateTime now = LocalDateTime.now();

        BudgetChange budgetChange = mock(BudgetChange.class);
        given(budgetChange.getId()).willReturn(10L);
        given(budgetChange.getChangedBudget()).willReturn(8000L);
        given(budgetChange.getType()).willReturn(BudgetType.SETTLEMENT);
        given(budgetChange.getReason()).willReturn("장비대여");
        given(budgetChange.getCreatedAt()).willReturn(now);

        BudgetHistoryResponse response = BudgetHistoryResponse.from(List.of(budgetChange));

        given(budgetService.getBudgetHistory(roomId)).willReturn(response);

        //when & then
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
