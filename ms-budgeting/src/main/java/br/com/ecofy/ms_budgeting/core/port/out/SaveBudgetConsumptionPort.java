package br.com.ecofy.ms_budgeting.core.port.out;

import br.com.ecofy.ms_budgeting.core.domain.BudgetConsumption;

public interface SaveBudgetConsumptionPort {

    BudgetConsumption save(BudgetConsumption consumption);

}