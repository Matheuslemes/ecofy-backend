package br.com.ecofy.ms_budgeting.adapters.out.persistence.repository;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, UUID> {

    long deleteByStatusAndArchivedAtLessThanEqual(String status, LocalDate cutoff);

}
