package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetHistoryResponse;
import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.service.BudgetService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
