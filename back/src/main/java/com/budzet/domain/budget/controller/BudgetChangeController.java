package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.*;
import com.budzet.domain.budget.service.BudgetChangeService;
import com.budzet.domain.user.entity.User;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03 - 3. 정산", description = "정산처리, 정산 목록/상세 조회 및 수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class BudgetChangeController {

    private final Rq rq;
    private final BudgetChangeService budgetChangeService;

    @Operation(
            summary = "정산 처리",
            description = "신청자 본인만 가능",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/{roomId}/budget/changes/{requestId}")
    public ResponseEntity<ApiResponse<BudgetChangeCreateResponse>> createBudgetChange(
            @PathVariable Long roomId,
            @PathVariable Long requestId,
            @RequestBody @Valid BudgetChangeCreateRequest request
            ){

        User user = rq.getActor();

        BudgetChangeCreateResponse response = budgetChangeService.createBudgetChange(
                roomId, user.getId(), requestId, request);

        return ApiResponse.response(HttpStatus.CREATED,"정산 처리 성공",response);

    }

    @Operation(
            summary = "정산 내역 목록 조회",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/{roomId}/budget/changes/settlement")
    public ResponseEntity<ApiResponse<BudgetChangeListResponse>> budgetChangeList(
            @PathVariable Long roomId
    ){
        User user = rq.getActor();
        BudgetChangeListResponse response = budgetChangeService.budgetChangeList(roomId, user.getId());
        return ApiResponse.response(HttpStatus.OK,"정산 내역 조회 성공",response);
    }

    @Operation(
            summary = "정산 내역 상세 조회",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/{roomId}/budget/changes/{changeId}")
    public ResponseEntity<ApiResponse<BudgetChangeDetailResponse>> budgetChangeDetail(
            @PathVariable Long roomId,
            @PathVariable Long changeId
    ){
        User user = rq.getActor();
        BudgetChangeDetailResponse response = budgetChangeService.budgetChangeDetail(roomId, user.getId(), changeId);
        return ApiResponse.response(HttpStatus.OK,"정산 내역 상세조회 성공",response);
    }

    @Operation(
            summary = "정산 내역 수정",
            description = "모임장 권한 필요",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PutMapping("/{roomId}/budget/changes/{changeId}")
    public ResponseEntity<ApiResponse<BudgetChangeUpdateResponse>> updateBudgetChange(
            @PathVariable Long roomId,
            @PathVariable Long changeId,
            @RequestBody @Valid BudgetChangeUpdateRequest request
    ){
        User user = rq.getActor();
        BudgetChangeUpdateResponse response = budgetChangeService.updateBudgetChange(
                roomId, user.getId(), changeId, request);

        return ApiResponse.response(HttpStatus.OK,"정산 내역 수정 성공",response);
    }
}
