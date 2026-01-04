package br.com.ecofy.ms_budgeting.core.application.service;

import br.com.ecofy.ms_budgeting.core.application.result.BudgetConsumptionResult;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetOverviewResult;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetNotFoundException;
import br.com.ecofy.ms_budgeting.core.port.in.GetBudgetOverviewUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.GetBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.ListBudgetsUseCase;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class BudgetQueryService implements ListBudgetsUseCase, GetBudgetUseCase, GetBudgetOverviewUseCase {

    private final LoadBudgetsPort loadBudgetsPort;

    public BudgetQueryService(LoadBudgetsPort loadBudgetsPort) {
        this.loadBudgetsPort = Objects.requireNonNull(loadBudgetsPort);
    }

    @Override
    public List<BudgetResult> listByUser(UUID userId) {
        return loadBudgetsPort.findByUserId(userId).stream()
                .map(BudgetCommandService::toResult)
                .toList();
    }

    @Override
    public BudgetResult get(UUID budgetId) {
        return loadBudgetsPort.findById(budgetId)
                .map(BudgetCommandService::toResult)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));
    }

    @Override
    public BudgetOverviewResult overview(UUID userId) {
        var budgets = loadBudgetsPort.findByUserId(userId);

        var consumptions = budgets.stream()
                .map(b -> new BudgetConsumptionResult(
                        b.getId(),
                        BigDecimal.ZERO,
                        b.getLimit().amount(),
                        BigDecimal.ZERO
                )).toList();

        return new BudgetOverviewResult(userId, consumptions, List.of());
    }
}
