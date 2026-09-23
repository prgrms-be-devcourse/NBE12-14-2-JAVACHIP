package com.budzet.domain.budget.controller;

import com.budzet.domain.budget.dto.BudgetRequestModifyRequest;
import com.budzet.domain.budget.dto.BudgetRequestRejectRequest;
import com.budzet.domain.budget.dto.BudgetRequestResponse;
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

    /**
     * 예산신청 등록
     * @param roomId
     * @param budgetRequestRequest
     * @return data 없음
     */
    @PostMapping("/{roomId}/budget/request")
    public ResponseEntity<ApiResponse<Object>> postBudgetRequest(
            @PathVariable Long roomId,
            @Valid @RequestBody BudgetRequestRequest budgetRequestRequest
    ){
        User user = rq.getActor();
        budgetRequestService.budgetRequestRegistration(roomId, user, budgetRequestRequest.reason(), budgetRequestRequest.requested_amount());
        return ApiResponse.response(HttpStatus.CREATED, "예산신청 등록성공", null);
    }

    /**
     * 현재방의 예산신청 목록조회
     * @param roomId
     * @return 예산신청 목록
     */
    @GetMapping("/{roomId}/budget/request/list")
    public ResponseEntity<ApiResponse<List<BudgetRequestResponse>>> getBudgetRequestList(
            @PathVariable Long roomId
    ){
        User user = rq.getActor();
        return ApiResponse.response(HttpStatus.OK, "예산신청 목록조회성공", budgetRequestService.getBudgetRequestList(roomId, user));
    }

    /**
     * 예산신청 상세조회
     * @param roomId
     * @param requestId
     * @return 예산신청 상세정보
     */
    @GetMapping("/{roomId}/budget/request/{requestId}")
    public ResponseEntity<ApiResponse<BudgetRequestResponse>> getBudgetRequest(
            @PathVariable Long roomId,
            @PathVariable Long requestId
    ){
        User user = rq.getActor();
        return ApiResponse.response(HttpStatus.OK, "예산신청 조회성공", budgetRequestService.getBudgetRequest(roomId, user, requestId));
    }

    /**
     * 예산신청 삭제
     * @param roomId
     * @param requestId
     * @return 예산신청 상세정보
     */
    @DeleteMapping("/{roomId}/budget/request/{requestId}")
    public ResponseEntity<ApiResponse<BudgetRequestResponse>> deleteBudgetRequest(
            @PathVariable Long roomId,
            @PathVariable Long requestId
    ){
        User user = rq.getActor();
        budgetRequestService.deleteBudgetRequest(roomId, user, requestId);
        return ApiResponse.response(HttpStatus.NO_CONTENT, "예산신청 삭제성공", null);
    }

    /**
     * 예산신청 승인
     * @param roomId
     * @param requestId
     * @return 예산신청 상세정보
     */
    @PatchMapping("/{roomId}/budget/request/{requestId}/approve")
    public ResponseEntity<ApiResponse<Object>> approveBudgetRequest(
            @PathVariable Long roomId,
            @PathVariable Long requestId
    ){
        User user = rq.getActor();
        budgetRequestService.approveBudgetRequest(roomId, user, requestId);
        return ApiResponse.response(HttpStatus.NO_CONTENT, "예산신청 승인성공", null);
    }

    /**
     * 예산신청 반려
     * @param roomId
     * @param requestId
     * @param request
     * @return 예산신청 상세정보
     */
    @PatchMapping("/{roomId}/budget/request/{requestId}/reject")
    public ResponseEntity<ApiResponse<Object>> rejectBudgetRequest(
            @PathVariable Long roomId,
            @PathVariable Long requestId,
            @Valid @RequestBody BudgetRequestRejectRequest request
    ){
        User user = rq.getActor();
        budgetRequestService.rejectBudgetRequest(roomId, user, requestId, request.rejectReason());
        return ApiResponse.response(HttpStatus.NO_CONTENT, "예산신청 반려성공", null);
    }

    /**
     * 예산신청 수정
     * @param roomId
     * @param requestId
     * @param request
     * @return 예산신청 상세정보
     */
    @PatchMapping("/{roomId}/budget/request/{requestId}/modify")
    public ResponseEntity<ApiResponse<Object>> modifyBudgetRequest(
            @PathVariable Long roomId,
            @PathVariable Long requestId,
            @Valid @RequestBody BudgetRequestModifyRequest request
    ){
        User user = rq.getActor();
        budgetRequestService.modifyBudgetRequest(roomId, user, requestId, request.reason(), request.rerequestedAmount());
        return ApiResponse.response(HttpStatus.NO_CONTENT, "예산신청 수정성공", null);
    }

}
