package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetRequestRequest;
import com.budzet.domain.budget.service.BudgetRequestService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.rq.Rq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
// 💡 요청하신 정확한 경로의 WebMvcTest 사용
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetRequestController.class)
class BudgetRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 💡 jpaAuditingHandler의 MappingContext 타입 캐스팅 요구 조건을 맞추기 위한 스프링 데이터 공통 인터페이스 선언
    @MockitoBean(name = "jpaMappingContext")
    private org.springframework.data.mapping.context.MappingContext<?, ?> jpaMappingContext;

    @MockitoBean
    private Rq rq;

    @MockitoBean
    private BudgetRequestService budgetRequestService;

    @Test
    @DisplayName("성공: 예산 신청 등록을 요청하면 HTTP 상태코드 201과 ApiResponse 구조를 정확히 반환한다.")
    void postBudgetRequest_Success() throws Exception {
        // given
        Long roomId = 10L;
        String reason = "비품 구매";
        Long requestedAmount = 50000L;

        // 요청 바디 데이터 객체 생성 (레코드 구조에 맞춤)
        BudgetRequestRequest requestDto = new BudgetRequestRequest(reason, requestedAmount);

        // 가짜 User 객체 생성 및 Rq 스터빙 설정
        User mockUser = Mockito.mock(User.class);
        when(rq.getActor()).thenReturn(mockUser);

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/budget/request", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value(201))
                .andExpect(jsonPath("$.message").value("예산신청 등록성공"))
                .andExpect(jsonPath("$.data").isEmpty());

        // 비즈니스 로직(서비스 호출)이 가짜 유저와 함께 정확한 인자로 실행되었는지 검증
        verify(budgetRequestService).budgetRequestRegistration(
                eq(roomId),
                eq(mockUser),
                eq(reason),
                eq(requestedAmount)
        );
    }
}
