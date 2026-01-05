package br.com.ecofy.ms_budgeting.adapters.in.sched;

import br.com.ecofy.ms_budgeting.core.application.command.RecalculateBudgetsCommand;
import br.com.ecofy.ms_budgeting.core.port.in.RecalculateBudgetsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetRecalculationScheduler {

    private final RecalculateBudgetsUseCase useCase;
    private final Clock clock; // injete Clock para testes determinísticos

    @Scheduled(cron = "${ecofy.budgeting.scheduling.recalculate-cron:0 0/15 * * * *}")
    public void recalc() {
        UUID runId = UUID.randomUUID();
        Instant start = Instant.now(clock);

        // Scheduler normalmente recalcula "global" (todos os usuários).
        // Então userId fica null (ou Optional) e o caso de uso interpreta como "para todos".
        LocalDate referenceDate = LocalDate.now(clock);

        log.info("[BudgetRecalculationScheduler] START runId={} referenceDate={}", runId, referenceDate);

        try {
            useCase.recalculate(new RecalculateBudgetsCommand(runId, null, referenceDate));

            Duration took = Duration.between(start, Instant.now(clock));
            log.info("[BudgetRecalculationScheduler] DONE runId={} tookMs={}", runId, took.toMillis());
        } catch (Exception ex) {
            Duration took = Duration.between(start, Instant.now(clock));
            log.error("[BudgetRecalculationScheduler] FAIL runId={} tookMs={} error={}",
                    runId, took.toMillis(), ex.getMessage(), ex);
        }
    }
}
