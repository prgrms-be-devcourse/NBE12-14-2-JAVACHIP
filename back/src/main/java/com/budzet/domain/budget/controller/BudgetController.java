package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.dto.BudgetUpdateRequest;
import com.budzet.domain.budget.dto.BudgetUpdateResponse;
import com.budzet.domain.budget.service.BudgetService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rooms")
public class BudgetController {

    private final BudgetService budgetService;
    private final Rq rq;

    @GetMapping("/{roomId}/budget")
    public ApiResponse<BudgetResponse> getBudget(
            @PathVariable Long roomId){

        User user = rq.getActor();

        BudgetResponse budgetResponse = budgetService.getBudget(roomId,user.getId());
        return ApiResponse.success(
                HttpStatus.OK,
                "예산 조회에 성공하였습니다.",
                budgetResponse);
    }


    @GetMapping("/{roomId}/budget/history")
    public ApiResponse<BudgetHistoryResponse> getBudgetHistory(
            @PathVariable Long roomId
    ){

        User user = rq.getActor();

        BudgetHistoryResponse budgetHistoryResponse = budgetService.getBudgetHistory(roomId, user.getId());
        return ApiResponse.success(
                HttpStatus.OK,
                "예산 변동 목록 조회에 성공하였습니다.",
                budgetHistoryResponse);
    }

    @PatchMapping("/{roomId}/budget")
    public ApiResponse<BudgetUpdateResponse> updateBudget(
            @PathVariable Long roomId,
            @RequestBody @Valid BudgetUpdateRequest budgetUpdateRequest
            ){

        User user = rq.getActor();

        BudgetUpdateResponse budgetUpdateResponse = budgetService.updateBudget(
                roomId,user.getId(),
                budgetUpdateRequest);

        return ApiResponse.success(
                HttpStatus.OK,
                "예산 수정에 성공하였습니다.",
                budgetUpdateResponse);
    }
}
