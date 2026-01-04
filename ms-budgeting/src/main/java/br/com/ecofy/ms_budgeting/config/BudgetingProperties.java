package br.com.ecofy.ms_budgeting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

@ConfigurationProperties(prefix = "ecofy.budgeting")
public record BudgetingProperties(

        Topics topics,
        Idempotency idempotency,
        Alerts alerts,
        Schedulers schedulers

) {
    public record Topics(
            String categorizedTransaction,
            String budgetAlert
    ) {}

    public record Idempotency(
            Duration ttl
    ) {}

    public record Alerts(
            BigDecimal warningThresholdPct,
            BigDecimal criticalThresholdPct,
            boolean publishOnEveryUpdate
    ) {}

    public record Schedulers(
            boolean recalculationEnabled,
            boolean cleanupEnabled
    ) {}

}