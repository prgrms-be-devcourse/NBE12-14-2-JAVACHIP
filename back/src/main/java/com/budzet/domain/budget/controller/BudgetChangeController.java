package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetChangeCreateRequest;
import com.budzet.domain.budget.dto.BudgetChangeCreateResponse;
import com.budzet.domain.budget.dto.BudgetChangeDetailResponse;
import com.budzet.domain.budget.dto.BudgetChangeListResponse;
import com.budzet.domain.budget.service.BudgetChangeService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class BudgetChangeController {

    private final Rq rq;
    private final BudgetChangeService budgetChangeService;

    @PostMapping("/{roomId}/budget/changes/{requestId}")
    public ResponseEntity<ApiResponse<BudgetChangeCreateResponse>> createBudgetChange(
            @PathVariable Long roomId,
            @PathVariable Long requestId,
            @RequestBody @Valid BudgetChangeCreateRequest request
            ){

        User user = rq.getActor();

        BudgetChangeCreateResponse response = budgetChangeService.createBudgetChange(
                roomId, user.getId(), requestId, request);

        return ApiResponse.response(HttpStatus.CREATED,"정산 내역 등록 성공",response);

    }

    @GetMapping("/{roomId}/budget/changes/settlement")
    public ResponseEntity<ApiResponse<BudgetChangeListResponse>> budgetChangeList(
            @PathVariable Long roomId
    ){
        User user = rq.getActor();
        BudgetChangeListResponse response = budgetChangeService.budgetChangeList(roomId, user.getId());
        return ApiResponse.response(HttpStatus.OK,"정산 내역 조회 성공",response);
    }

    @GetMapping("/{roomId}/budget/changes/{changeId}")
    public ResponseEntity<ApiResponse<BudgetChangeDetailResponse>> budgetChangeDetail(
            @PathVariable Long roomId,
            @PathVariable Long changeId
    ){
        User user = rq.getActor();
        BudgetChangeDetailResponse response = budgetChangeService.budgetChangeDetail(roomId, user.getId(), changeId);
        return ApiResponse.response(HttpStatus.OK,"정산 내역 상세조회 성공",response);
    }
}
