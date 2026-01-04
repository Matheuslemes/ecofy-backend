package br.com.ecofy.ms_budgeting.adapters.in.sched;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetCleanupScheduler {

    @Scheduled(cron = "${ecofy.budgeting.scheduling.cleanup-cron:0 0 3 * * *}")
    public void cleanup() {
        log.info("[BudgetCleanupScheduler] - [cleanup] -> NOOP (placeholder)");
    }

}