package br.com.ecofy.ms_budgeting.adapters.out.persistence.repository;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlertEntity, UUID> {

    List<BudgetAlertEntity> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

}
