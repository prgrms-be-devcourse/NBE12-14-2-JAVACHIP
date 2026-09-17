package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetRequestRequest;
import com.budzet.domain.budget.service.BudgetRequestService;
import com.budzet.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class BudgetRequestController {

    private BudgetRequestService budgetRequestService;

    @PostMapping("/{roomId}/budget/request")
    public ApiResponse<String> postBudgetRequest(
            @PathVariable Long roomId,
            @RequestBody BudgetRequestRequest budgetRequestRequest
    ){
        Long userIdProxy = 1L;
        budgetRequestService.budgetRequestRegistration(roomId, userIdProxy, budgetRequestRequest.reason(), budgetRequestRequest.requested_amount());
        return ApiResponse.success(HttpStatus.CREATED, "예산신청 등록성공", "");
    }

}
