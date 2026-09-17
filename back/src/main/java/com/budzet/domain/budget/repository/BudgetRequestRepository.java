package com.budzet.domain.budget.repository;

import com.budzet.domain.budget.entity.BudgetRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRequestRepository extends JpaRepository<BudgetRequest, Long> {
}
