package com.budzet.domain.budget.repository;

import com.budzet.domain.budget.dto.BudgetRequestListResponse;
import com.budzet.domain.budget.entity.BudgetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetRequestRepository extends JpaRepository<BudgetRequest, Long> {

    @Query("select new com.budzet.domain.budget.dto.BudgetRequestListResponse(" +
            "br.room.id, br.user.id, br.reason, br.requestedAmount, br.status, br.rejectReason, br.createdAt, br.updatedAt) " +
            "from BudgetRequest br " +
            "where br.room.id = :roomId")
    List<BudgetRequestListResponse> findDtoByRoomId(@Param("roomId") Long roomId);

}
