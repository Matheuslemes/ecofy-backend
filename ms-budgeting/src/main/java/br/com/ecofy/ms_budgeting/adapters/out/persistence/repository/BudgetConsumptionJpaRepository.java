package br.com.ecofy.ms_budgeting.adapters.out.persistence.repository;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetConsumptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BudgetConsumptionJpaRepository extends JpaRepository<BudgetConsumptionEntity, Long> {

    long deleteByReferenceDateLessThanEqual(LocalDate cutoff);

}