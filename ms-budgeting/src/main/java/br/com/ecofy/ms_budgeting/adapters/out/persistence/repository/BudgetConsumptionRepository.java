package br.com.ecofy.ms_budgeting.adapters.out.persistence.repository;


import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetConsumptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BudgetConsumptionRepository extends JpaRepository<BudgetConsumptionEntity, UUID> {

    Optional<BudgetConsumptionEntity> findTopByBudgetIdOrderByUpdatedAtDesc(UUID budgetId);

}