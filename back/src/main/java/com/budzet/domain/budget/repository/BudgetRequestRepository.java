package com.budzet.domain.budget.repository;

import com.budzet.domain.budget.dto.BudgetRequestListResponse;
import com.budzet.domain.budget.entity.BudgetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetRequestRepository extends JpaRepository<BudgetRequest, Long> {

    /**
     * roomId가 일치하는 예산신청 목록반환, roomId, userId 직접반환처리
     * @param roomId
     * @return 예산신청 목록
     */
    @Query("select new com.budzet.domain.budget.dto.BudgetRequestListResponse(" +
            "br.room.id, br.user.id, br.reason, br.requestedAmount, br.status, br.rejectReason, br.createdAt, br.updatedAt) " +
            "from BudgetRequest br " +
            "where br.room.id = :roomId")
    List<BudgetRequestListResponse> findDtoByRoomId(@Param("roomId") Long roomId);

    /**
     * roomId와 userId가 일치하는 예산신청 단일항목반환, roomId, userId 직접반환처리
     * @param roomId
     * @param requestId
     * @return 예산신청 단일항목
     */
    @Query("select new com.budzet.domain.budget.dto.BudgetRequestListResponse(" +
            "br.room.id, br.user.id, br.reason, br.requestedAmount, br.status, br.rejectReason, br.createdAt, br.updatedAt) " +
            "from BudgetRequest br " +
            "where br.room.id = :roomId and br.id = :requestId")
    BudgetRequestListResponse findDtoById(@Param("roomId") Long roomId, @Param("requestId") Long requestId);

}
