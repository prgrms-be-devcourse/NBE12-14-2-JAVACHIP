package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetRequestListResponse;
import com.budzet.domain.budget.dto.BudgetRequestRequest;
import com.budzet.domain.budget.service.BudgetRequestService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class BudgetRequestController {

    private final Rq rq;
    private final BudgetRequestService budgetRequestService;

    @PostMapping("/{roomId}/budget/request")
    public ResponseEntity<ApiResponse<Object>> postBudgetRequest(
            @PathVariable Long roomId,
            @Valid @RequestBody BudgetRequestRequest budgetRequestRequest
    ){
        User user = rq.getActor();
        budgetRequestService.budgetRequestRegistration(roomId, user, budgetRequestRequest.reason(), budgetRequestRequest.requested_amount());
        return ApiResponse.response(HttpStatus.CREATED, "예산신청 등록성공", null);
    }

    @GetMapping("/{roomId}/budget/request/list")
    public ResponseEntity<ApiResponse<List<BudgetRequestListResponse>>> getBudgetRequestList(
            @PathVariable Long roomId
    ){
        User user = rq.getActor();
        return ApiResponse.response(HttpStatus.OK, "예산신청 목록조회성공", budgetRequestService.getBudgetRequestList(roomId, user));
    }

}
