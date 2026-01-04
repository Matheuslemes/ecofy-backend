package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;

import java.util.List;
import java.util.UUID;

public interface ListBudgetsUseCase {

    List<BudgetResult> listByUser(UUID userId);

}
