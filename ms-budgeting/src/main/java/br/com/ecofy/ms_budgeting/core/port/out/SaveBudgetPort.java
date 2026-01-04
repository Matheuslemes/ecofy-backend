package br.com.ecofy.ms_budgeting.core.port.out;

import br.com.ecofy.ms_budgeting.core.domain.Budget;

public interface SaveBudgetPort {

    Budget save(Budget budget);

}
