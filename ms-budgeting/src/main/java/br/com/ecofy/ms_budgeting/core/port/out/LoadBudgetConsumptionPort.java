package br.com.ecofy.ms_budgeting.core.port.out;

import br.com.ecofy.ms_budgeting.core.domain.BudgetConsumption;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface LoadBudgetConsumptionPort {

    Optional<BudgetConsumption> findByBudgetAndPeriod(UUID budgetId, LocalDate start, LocalDate end);

}