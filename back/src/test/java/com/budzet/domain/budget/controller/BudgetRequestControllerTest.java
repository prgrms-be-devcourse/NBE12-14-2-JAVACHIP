package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetRequestRequest;
import com.budzet.domain.budget.service.BudgetRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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

    // JPA Auditing 관련 컨텍스트 로딩 에러 방지용 가짜 빈
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // 비즈니스 로직(Service) 격리용 가짜 빈
    @MockitoBean
    private BudgetRequestService budgetRequestService;

    @Test
    @DisplayName("성공: 예산 신청 등록을 요청하면 HTTP 상태코드 201과 ApiResponse 구조를 정확히 반환한다.")
    void postBudgetRequest_Success() throws Exception {
        // given
        Long roomId = 10L;
        Long expectedUserId = 1L; // 컨트롤러 내부에 하드코딩된 userIdProxy 값
        BudgetRequestRequest requestDto = new BudgetRequestRequest("비품 구매", 50000L);

        // void 타입의 서비스 메서드가 예외 없이 정상 수행되도록 설정
        doNothing().when(budgetRequestService)
                .budgetRequestRegistration(
                        eq(roomId),
                        eq(expectedUserId),
                        eq(requestDto.reason()),
                        eq(requestDto.requested_amount())
                );

        // when & then
        mockMvc.perform(post("/rooms/{roomId}/budget/request", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print()) // 상세 HTTP 요청/응답 로그 출력

                // 1. HTTP 네트워크 응답 규격상 상태 코드 201(Created) 검증
                .andExpect(status().isCreated())

                // 2. ApiResponse 바디 객체 내부의 알맹이 구조 및 데이터 검증
                .andExpect(jsonPath("$.resultCode").value(201))
                .andExpect(jsonPath("$.message").value("예산신청 등록성공"))
                .andExpect(jsonPath("$.data").isEmpty()); // null 데이터 검증

        // then: 컨트롤러가 설계된 인자 값을 토대로 서비스 레이어를 정상 호출했는지 확인
        verify(budgetRequestService).budgetRequestRegistration(
                eq(roomId),
                eq(expectedUserId),
                eq(requestDto.reason()),
                eq(requestDto.requested_amount())
        );
    }
}
