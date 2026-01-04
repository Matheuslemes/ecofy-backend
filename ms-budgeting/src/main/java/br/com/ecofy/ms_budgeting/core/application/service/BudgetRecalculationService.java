package br.com.ecofy.ms_budgeting.core.application.service;

import br.com.ecofy.ms_budgeting.core.application.command.RecalculateBudgetsCommand;
import br.com.ecofy.ms_budgeting.core.port.in.RecalculateBudgetsUseCase;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetConsumptionPort;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetRecalculationService implements RecalculateBudgetsUseCase {

    private final LoadBudgetsPort loadBudgetsPort;
    private final LoadBudgetConsumptionPort loadBudgetConsumptionPort;

    @Override
    public void recalculate(RecalculateBudgetsCommand cmd) {
        log.info("[BudgetRecalculationService] - [recalculate] -> runId={}", cmd.runId());

        var budgets = loadBudgetsPort.loadAllActive();
        for (var b : budgets) {
            var consumption = loadBudgetConsumptionPort.getCurrentConsumption(b.getId());
            log.debug("[BudgetRecalculationService] - budgetId={} consumedCents={}",
                    b.getId(), consumption == null ? null : consumption.getConsumed().cents());
        }
    }

}