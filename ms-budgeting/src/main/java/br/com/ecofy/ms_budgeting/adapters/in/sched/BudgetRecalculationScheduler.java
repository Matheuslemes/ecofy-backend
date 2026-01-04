package br.com.ecofy.ms_budgeting.adapters.in.sched;

import br.com.ecofy.ms_budgeting.core.application.command.RecalculateBudgetsCommand;
import br.com.ecofy.ms_budgeting.core.port.in.RecalculateBudgetsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BudgetRecalculationScheduler {

    private final RecalculateBudgetsUseCase useCase;

    @Scheduled(cron = "${ecofy.budgeting.scheduling.recalculate-cron:0 0/15 * * * *}")
    public void recalc() {
        String runId = UUID.randomUUID().toString();
        log.info("[BudgetRecalculationScheduler] - [recalc] -> START runId={}", runId);

        useCase.recalculate(new RecalculateBudgetsCommand(runId));

        log.info("[BudgetRecalculationScheduler] - [recalc] -> DONE runId={}", runId);
    }

}
