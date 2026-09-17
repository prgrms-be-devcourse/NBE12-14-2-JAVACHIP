package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetResponse;
import com.budzet.domain.budget.service.BudgetService;
import com.budzet.global.api.ApiResponse;
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

    @GetMapping("/{roomId}/budget")
    public ApiResponse<BudgetResponse> getBudget(
            @PathVariable Long roomId){

        BudgetResponse budgetResponse = this.budgetService.getBudget(roomId);
        return ApiResponse.success(
                HttpStatus.OK,
                "예산 조회에 성공하였습니다.",
                budgetResponse);
    }
}
