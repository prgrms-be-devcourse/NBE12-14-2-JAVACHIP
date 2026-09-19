package com.budzet.domain.budget.repository;

import com.budzet.domain.budget.entity.BudgetChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetChangeRepository extends JpaRepository<BudgetChange, Long> {

    List<BudgetChange> findAllByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<BudgetChange> findAllByRoomIdAndTypeOrderByCreatedAtDesc(Long roomId, BudgetType type);
}
