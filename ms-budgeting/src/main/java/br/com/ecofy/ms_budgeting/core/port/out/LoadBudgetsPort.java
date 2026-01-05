package br.com.ecofy.ms_budgeting.core.port.out;

import br.com.ecofy.ms_budgeting.core.domain.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadBudgetsPort {

    Optional<Budget> findById(UUID id);

    List<Budget> findByUserId(UUID userId);

    boolean existsByNaturalKey(String naturalKey);

    /**
     * Usado por rotinas batch/scheduler (recalculation/cleanup).
     * Retorna apenas budgets em estado ACTIVE.
     */
    List<Budget> findAllActive();
}
