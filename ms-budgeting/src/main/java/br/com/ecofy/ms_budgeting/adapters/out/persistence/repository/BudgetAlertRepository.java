package br.com.ecofy.ms_budgeting.adapters.out.persistence.repository;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.BudgetAlertEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlertEntity, UUID> {

    @Query("""
        select a
        from BudgetAlertEntity a
        join BudgetEntity b on b.id = a.budgetId
        where b.userId = :userId
        order by a.createdAt desc
    """)
    List<BudgetAlertEntity> findTop50ByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

}

