package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetRequestRequest;
import com.budzet.domain.budget.service.BudgetRequestService;
import com.budzet.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class BudgetRequestController {

    private final BudgetRequestService budgetRequestService;

    @PostMapping("/{roomId}/budget/request")
    public ResponseEntity<ApiResponse<Object>> postBudgetRequest(
            @PathVariable Long roomId,
            @Valid @RequestBody BudgetRequestRequest budgetRequestRequest
    ){
        Long userIdProxy = 1L;
        budgetRequestService.budgetRequestRegistration(roomId, userIdProxy, budgetRequestRequest.reason(), budgetRequestRequest.requested_amount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "예산신청 등록성공", null));
    }

}
