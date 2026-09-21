package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.*;
import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.domain.budget.service.BudgetChangeService;
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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "JWT_SECRET=test_jwt_secret_key_12345678901234567890")
@AutoConfigureMockMvc
public class BudgetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private BudgetChangeService budgetChangeService;

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
        given(budgetChange.getUserName()).willReturn("홍길동");
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
                .andExpect(jsonPath("$.data.history[0].userName").value("홍길동"))
                .andExpect(jsonPath("$.data.history[0].reason").value("장비대여"))
                .andExpect(jsonPath("$.data.history[0].reason").exists());
    }

    @Test
    @DisplayName("예산 수정 성공")
    void updateBudget_success() throws Exception {

        // given
        Long roomId = 1L;
        BudgetUpdateRequest request = new BudgetUpdateRequest(5000L, BudgetType.INCREASE, "지원금 추가");
        BudgetUpdateResponse response = new BudgetUpdateResponse(roomId, 15000L);

        given(budgetService.updateBudget(eq(roomId), eq(testUserId), any(BudgetUpdateRequest.class)))
                .willReturn(response);

        // when & then
        mvc.perform(patch("/rooms/{roomId}/budget", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.message").value("예산 수정에 성공하였습니다."))
                .andExpect(jsonPath("$.data.roomId").value(1L))
                .andExpect(jsonPath("$.data.totalBudget").value(15000L));
    }

    @Test
    @DisplayName("예산 수정 실패 - 금액이 0원 이하일 때 Validation 예외 발생")
    void updateBudget_validationError_invalidAmount() throws Exception {

        // given
        Long roomId = 1L;
        // @Positive 조건 위반 (0 이하 금액)
        BudgetUpdateRequest invalidRequest = new BudgetUpdateRequest(-1000L, BudgetType.INCREASE, "잘못된 금액");

        // when & then
        mvc.perform(patch("/rooms/{roomId}/budget", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // 400 Bad Request 검증
    }

    @Test
    @DisplayName("예산 수정 실패 - 변동 유형(BudgetType)이 Null일 때 Validation 예외 발생")
    void updateBudget_validationError_nullType() throws Exception {

        // given
        Long roomId = 1L;
        // @NotNull 조건 위반 (BudgetType null)
        BudgetUpdateRequest invalidRequest = new BudgetUpdateRequest(1000L, null, "타입 없음");

        // when & then
        mvc.perform(patch("/rooms/{roomId}/budget", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // 400 Bad Request 검증
    }

    @Test
    @DisplayName("예산 변경(정산) 내역 생성 성공 - 201 CREATED")
    void createBudgetChange_success() throws Exception {

        // given
        Long roomId = 1L;
        Long requestId = 10L;
        Long changeId = 50L;
        LocalDateTime now = LocalDateTime.now();

        // 1. 서비스에 넘길 요청 DTO (프로젝트 필드명에 맞게 조정 필요)
        BudgetChangeCreateRequest request = new BudgetChangeCreateRequest(5000L, "회식비 정산");

        // 2. BudgetChange Mock 및 응답 DTO 생성
        BudgetChange budgetChange = mock(BudgetChange.class);
        BudgetRequest budgetRequest = mock(BudgetRequest.class);
        User user = mock(User.class);

        given(budgetRequest.getId()).willReturn(requestId);
        given(user.getId()).willReturn(testUserId);

        given(budgetChange.getId()).willReturn(changeId);
        given(budgetChange.getRequest()).willReturn(budgetRequest);
        given(budgetChange.getUser()).willReturn(user);
        given(budgetChange.getChangeBudget()).willReturn(5000L);
        given(budgetChange.getChangedBudget()).willReturn(9500L);
        given(budgetChange.getType()).willReturn(BudgetType.SETTLEMENT);
        given(budgetChange.getUserName()).willReturn("홍길동");
        given(budgetChange.getCreatedAt()).willReturn(now);
        given(budgetChange.getReason()).willReturn("팀 회식 신청");

        BudgetChangeCreateResponse response = BudgetChangeCreateResponse.from(budgetChange);

        given(budgetChangeService.createBudgetChange(
                eq(roomId), eq(testUserId), eq(requestId), any(BudgetChangeCreateRequest.class)))
                .willReturn(response);

        // when & then
        mvc.perform(post("/rooms/{roomId}/budget/changes/{requestId}", roomId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value(201))
                .andExpect(jsonPath("$.message").value("정산 내역 등록 성공"))
                .andExpect(jsonPath("$.data.id").value(changeId))
                .andExpect(jsonPath("$.data.requestId").value(requestId))
                .andExpect(jsonPath("$.data.userId").value(testUserId))
                .andExpect(jsonPath("$.data.changeBudget").value(5000L))
                .andExpect(jsonPath("$.data.changedBudget").value(9500L))
                .andExpect(jsonPath("$.data.budgetType").value("SETTLEMENT"))
                .andExpect(jsonPath("$.data.userName").value("홍길동"))
                .andExpect(jsonPath("$.data.reason").value("팀 회식 신청"));
    }

    @Test
    @DisplayName("예산 변경 생성 실패 - 정산금액이 0 이하일 때 Validation 예외 발생 (400)")
    void createBudgetChange_validationError_negativeAmount() throws Exception {
        // given
        Long roomId = 1L;
        Long requestId = 10L;
        BudgetChangeCreateRequest invalidRequest = new BudgetChangeCreateRequest(-1000L, "사유");

        // when & then
        mvc.perform(post("/rooms/{roomId}/budget/changes/{requestId}", roomId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("정산 내역 목록 조회 성공 - 200 OK")
    void getMyBudgetChanges_success() throws Exception {
        // given
        Long roomId = 1L;
        LocalDateTime now = LocalDateTime.now();

        BudgetChange budgetChange = mock(BudgetChange.class);

        given(budgetChange.getId()).willReturn(50L);
        given(budgetChange.getChangedBudget()).willReturn(9500L);
        given(budgetChange.getType()).willReturn(BudgetType.SETTLEMENT);
        given(budgetChange.getUserName()).willReturn("홍길동");
        given(budgetChange.getCreatedAt()).willReturn(now);
        given(budgetChange.getReason()).willReturn("팀 회식 신청");

        BudgetChangeListResponse response = BudgetChangeListResponse.from(List.of(budgetChange));

        given(budgetChangeService.budgetChangeList(eq(roomId), eq(testUserId)))
                .willReturn(response);

        // when & then
        mvc.perform(get("/rooms/{roomId}/budget/changes/settlement", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.message").value("정산 내역 조회 성공"))
                .andExpect(jsonPath("$.data.changes[0].id").value(50L))
                .andExpect(jsonPath("$.data.changes[0].changedBudget").value(9500L))
                .andExpect(jsonPath("$.data.changes[0].type").value("SETTLEMENT"))
                .andExpect(jsonPath("$.data.changes[0].userName").value("홍길동"))
                .andExpect(jsonPath("$.data.changes[0].processedAt").exists())
                .andExpect(jsonPath("$.data.changes[0].reason").value("팀 회식 신청"));
    }

    @Test
    @DisplayName("본인 정산 내역 목록 조회 성공 - 내역이 없는 경우 빈 배열 반환")
    void getMyBudgetChanges_emptyList() throws Exception {
        // given
        Long roomId = 1L;
        BudgetChangeListResponse emptyResponse = BudgetChangeListResponse.from(List.of());

        given(budgetChangeService.budgetChangeList(eq(roomId), eq(testUserId)))
                .willReturn(emptyResponse);

        // when & then
        mvc.perform(get("/rooms/{roomId}/budget/changes/settlement", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.message").value("정산 내역 조회 성공"))
                .andExpect(jsonPath("$.data.changes").isEmpty());
    }
}