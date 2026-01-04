package br.com.ecofy.ms_budgeting.core.application.service;


import br.com.ecofy.ms_budgeting.config.BudgetingProperties;
import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;
import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;
import br.com.ecofy.ms_budgeting.core.domain.BudgetConsumption;
import br.com.ecofy.ms_budgeting.core.domain.enums.AlertSeverity;
import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import br.com.ecofy.ms_budgeting.core.domain.enums.ConsumptionSource;
import br.com.ecofy.ms_budgeting.core.domain.valueobject.Money;
import br.com.ecofy.ms_budgeting.core.domain.valueobject.Period;
import br.com.ecofy.ms_budgeting.core.port.in.ProcessTransactionForBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.out.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class BudgetProjectionService implements ProcessTransactionForBudgetUseCase {

    private final LoadBudgetsPort loadBudgetsPort;
    private final LoadBudgetConsumptionPort loadBudgetConsumptionPort;
    private final SaveBudgetConsumptionPort saveBudgetConsumptionPort;
    private final SaveBudgetAlertPort saveBudgetAlertPort;
    private final PublishBudgetAlertEventPort publishBudgetAlertEventPort;
    private final IdempotencyPort idempotencyPort;
    private final BudgetingProperties props;

    public BudgetProjectionService(LoadBudgetsPort loadBudgetsPort,
                                   LoadBudgetConsumptionPort loadBudgetConsumptionPort,
                                   SaveBudgetConsumptionPort saveBudgetConsumptionPort,
                                   SaveBudgetAlertPort saveBudgetAlertPort,
                                   PublishBudgetAlertEventPort publishBudgetAlertEventPort,
                                   IdempotencyPort idempotencyPort,
                                   BudgetingProperties props) {
        this.loadBudgetsPort = Objects.requireNonNull(loadBudgetsPort);
        this.loadBudgetConsumptionPort = Objects.requireNonNull(loadBudgetConsumptionPort);
        this.saveBudgetConsumptionPort = Objects.requireNonNull(saveBudgetConsumptionPort);
        this.saveBudgetAlertPort = Objects.requireNonNull(saveBudgetAlertPort);
        this.publishBudgetAlertEventPort = Objects.requireNonNull(publishBudgetAlertEventPort);
        this.idempotencyPort = Objects.requireNonNull(idempotencyPort);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public void process(ProcessTransactionCommand cmd) {
        String idemKey = "kafka:categorized-tx:" + cmd.transactionId();
        if (!idempotencyPort.tryAcquire(idemKey, props.idempotency().ttl(), "kafka:categorizedTx")) {
            log.warn("[BudgetProjectionService] - [process] -> Idempotency hit txId={}", cmd.transactionId());
            return;
        }

        var budgets = loadBudgetsPort.findByUserId(cmd.userId());
        if (budgets.isEmpty()) {
            log.debug("[BudgetProjectionService] - [process] -> No budgets for userId={}", cmd.userId());
            return;
        }

        Currency currency = Currency.getInstance(cmd.currency());
        Money amount = new Money(cmd.amount(), currency);

        for (var b : budgets) {
            if (b.getStatus() != BudgetStatus.ACTIVE) continue;
            if (!b.getKey().categoryId().value().equals(cmd.categoryId())) continue;
            if (!b.getKey().period().contains(cmd.transactionDate())) continue;

            upsertConsumptionAndAlerts(b.getId(), b.getLimit(), amount, cmd.transactionDate());
        }
    }

    private void upsertConsumptionAndAlerts(UUID budgetId, Money limit, Money delta, LocalDate txDate) {
        Instant now = Instant.now();

        var period = new Period(txDate.withDayOfMonth(1), txDate.withDayOfMonth(txDate.lengthOfMonth()));

        BudgetConsumption consumption = loadBudgetConsumptionPort
                .findByBudgetAndPeriod(budgetId, period.start(), period.end())
                .orElseGet(() -> new BudgetConsumption(
                        UUID.randomUUID(),
                        budgetId,
                        period.start(),
                        period.end(),
                        Money.zero(limit.currency()),
                        ConsumptionSource.CATEGORIZED_TX,
                        now,
                        now
                ));

        consumption.add(delta, now);
        BudgetConsumption saved = saveBudgetConsumptionPort.save(consumption);

        BigDecimal consumed = saved.getConsumed().amount();
        BigDecimal pct = consumed.multiply(BigDecimal.valueOf(100))
                .divide(limit.amount(), 2, java.math.RoundingMode.HALF_UP);

        AlertSeverity severity = resolveSeverity(pct);
        if (severity == null) {
            if (props.alerts().publishOnEveryUpdate()) {
                log.debug("[BudgetProjectionService] - [upsert] -> Updated consumption budgetId={} pct={}", budgetId, pct);
            }
            return;
        }

        String msg = "Budget threshold reached: " + pct + "% consumed for period " + saved.getPeriodStart() + " -> " + saved.getPeriodEnd();
        BudgetAlert alert = new BudgetAlert(
                UUID.randomUUID(),
                budgetId,
                saved.getId(),
                severity,
                msg,
                saved.getPeriodStart(),
                saved.getPeriodEnd(),
                now
        );

        BudgetAlert persisted = saveBudgetAlertPort.save(alert);
        publishBudgetAlertEventPort.publish(persisted);

        log.info("[BudgetProjectionService] - [alert] -> budgetId={} severity={} pct={}", budgetId, severity, pct);
    }

    private AlertSeverity resolveSeverity(BigDecimal pct) {
        if (pct.compareTo(props.alerts().criticalThresholdPct()) >= 0) return AlertSeverity.CRITICAL;
        if (pct.compareTo(props.alerts().warningThresholdPct()) >= 0) return AlertSeverity.WARNING;
        return null;
    }

}