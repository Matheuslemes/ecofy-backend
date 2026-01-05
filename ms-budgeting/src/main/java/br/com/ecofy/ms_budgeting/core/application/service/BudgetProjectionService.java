package br.com.ecofy.ms_budgeting.core.application.service;

import br.com.ecofy.ms_budgeting.config.BudgetingProperties;
import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;
import br.com.ecofy.ms_budgeting.core.domain.Budget;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class BudgetProjectionService implements ProcessTransactionForBudgetUseCase {

    private static final String SCOPE_KAFKA_CATEGORIZED_TX = "kafka:categorizedTx";

    private final LoadBudgetsPort loadBudgetsPort;
    private final LoadBudgetConsumptionPort loadBudgetConsumptionPort;
    private final SaveBudgetConsumptionPort saveBudgetConsumptionPort;
    private final SaveBudgetAlertPort saveBudgetAlertPort;
    private final PublishBudgetAlertEventPort publishBudgetAlertEventPort;
    private final IdempotencyPort idempotencyPort;
    private final BudgetingProperties props;
    private final Clock clock;

    public BudgetProjectionService(
            LoadBudgetsPort loadBudgetsPort,
            LoadBudgetConsumptionPort loadBudgetConsumptionPort,
            SaveBudgetConsumptionPort saveBudgetConsumptionPort,
            SaveBudgetAlertPort saveBudgetAlertPort,
            PublishBudgetAlertEventPort publishBudgetAlertEventPort,
            IdempotencyPort idempotencyPort,
            BudgetingProperties props,
            Clock clock
    ) {
        this.loadBudgetsPort = Objects.requireNonNull(loadBudgetsPort, "loadBudgetsPort must not be null");
        this.loadBudgetConsumptionPort = Objects.requireNonNull(loadBudgetConsumptionPort, "loadBudgetConsumptionPort must not be null");
        this.saveBudgetConsumptionPort = Objects.requireNonNull(saveBudgetConsumptionPort, "saveBudgetConsumptionPort must not be null");
        this.saveBudgetAlertPort = Objects.requireNonNull(saveBudgetAlertPort, "saveBudgetAlertPort must not be null");
        this.publishBudgetAlertEventPort = Objects.requireNonNull(publishBudgetAlertEventPort, "publishBudgetAlertEventPort must not be null");
        this.idempotencyPort = Objects.requireNonNull(idempotencyPort, "idempotencyPort must not be null");
        this.props = Objects.requireNonNull(props, "props must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    @Transactional
    public void process(ProcessTransactionCommand cmd) {
        Objects.requireNonNull(cmd, "cmd must not be null");
        validate(cmd);

        // Idempotência: prefira eventId (garante "exactly-once lógico" por evento Kafka)
        String eventId = cmd.metadata() != null ? cmd.metadata().eventId() : null;
        String idemKey = buildIdempotencyKey(eventId, String.valueOf(cmd.transactionId()));

        boolean acquired = idempotencyPort.tryAcquire(idemKey, props.idempotency().ttl(), SCOPE_KAFKA_CATEGORIZED_TX);
        if (!acquired) {
            log.warn("[BudgetProjectionService] - [process] -> Idempotency hit key={} txId={}", idemKey, cmd.transactionId());
            return;
        }

        List<Budget> budgets = loadBudgetsPort.findByUserId(UUID.fromString(String.valueOf(cmd.userId())));
        if (budgets.isEmpty()) {
            log.debug("[BudgetProjectionService] - [process] -> No budgets for userId={}", cmd.userId());
            return;
        }

        Currency currency = Currency.getInstance(cmd.currency().trim());
        Money delta = new Money(cmd.amount(), currency);
        LocalDate txDate = cmd.transactionDate();

        for (Budget b : budgets) {
            if (b.getStatus() != BudgetStatus.ACTIVE) continue;
            if (!b.getKey().categoryId().value().equals(cmd.categoryId())) continue;

            Period budgetPeriod = b.getKey().period();
            if (!budgetPeriod.contains(txDate)) continue;

            upsertConsumptionAndMaybeAlert(b, delta, txDate, eventId);
        }
    }

    private void upsertConsumptionAndMaybeAlert(Budget budget, Money delta, LocalDate txDate, String eventId) {
        Instant now = Instant.now(clock);

        Period period = budget.getKey().period();
        UUID budgetId = budget.getId();
        Money limit = budget.getLimit();

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

        BigDecimal before = consumption.getConsumed().amount();
        consumption.add(delta, now);
        BudgetConsumption saved = saveBudgetConsumptionPort.save(consumption);

        BigDecimal after = saved.getConsumed().amount();
        BigDecimal pctBefore = toPct(before, limit.amount());
        BigDecimal pctAfter = toPct(after, limit.amount());

        AlertSeverity severityAfter = resolveSeverity(pctAfter);
        if (severityAfter == null) {
            if (props.alerts().publishOnEveryUpdate()) {
                log.debug("[BudgetProjectionService] - [consumption] -> budgetId={} pctAfter={} amountAfter={}",
                        budgetId, pctAfter, after);
            }
            return;
        }

        // Evita alert spam: dispara apenas quando cruza o threshold (ex.: de <warning para >=warning)
        AlertSeverity severityBefore = resolveSeverity(pctBefore);
        boolean crossed = severityBefore != severityAfter;

        if (!crossed && !props.alerts().publishOnEveryUpdate()) {
            log.debug("[BudgetProjectionService] - [alert] -> suppressed (already in same severity) budgetId={} severity={} pctAfter={}",
                    budgetId, severityAfter, pctAfter);
            return;
        }

        // Dedup opcional por budget+consumption+severity (evita duplicar alertas em retries)
        String alertIdemKey = "alert:" + budgetId + ":" + saved.getId() + ":" + severityAfter;
        if (!idempotencyPort.tryAcquire(alertIdemKey, props.idempotency().ttl(), "budget:alert")) {
            log.debug("[BudgetProjectionService] - [alert] -> idempotency hit budgetId={} severity={}", budgetId, severityAfter);
            return;
        }

        String msg = buildAlertMessage(severityAfter, pctAfter, saved.getPeriodStart(), saved.getPeriodEnd());

        BudgetAlert alert = new BudgetAlert(
                UUID.randomUUID(),
                budgetId,
                saved.getId(),
                severityAfter,
                msg,
                saved.getPeriodStart(),
                saved.getPeriodEnd(),
                now
        );

        BudgetAlert persisted = saveBudgetAlertPort.save(alert);
        publishBudgetAlertEventPort.publish(persisted);

        log.info("[BudgetProjectionService] - [alert] -> budgetId={} severity={} pctAfter={} txDate={} eventId={}",
                budgetId, severityAfter, pctAfter, txDate, eventId);
    }

    private static BigDecimal toPct(BigDecimal consumed, BigDecimal limit) {
        if (limit == null || limit.signum() <= 0) {
            // Evita ArithmeticException; limite inválido não deveria existir no domínio, mas protege o boundary
            return BigDecimal.ZERO;
        }
        if (consumed == null) return BigDecimal.ZERO;

        return consumed
                .multiply(BigDecimal.valueOf(100))
                .divide(limit, 2, RoundingMode.HALF_UP);
    }

    private AlertSeverity resolveSeverity(BigDecimal pct) {
        if (pct == null) return null;

        if (pct.compareTo(props.alerts().criticalThresholdPct()) >= 0) return AlertSeverity.CRITICAL;
        if (pct.compareTo(props.alerts().warningThresholdPct()) >= 0) return AlertSeverity.WARNING;
        return null;
    }

    private static String buildAlertMessage(AlertSeverity severity, BigDecimal pct, LocalDate start, LocalDate end) {
        return "Budget " + severity + ": " + pct + "% consumed for period " + start + " -> " + end;
    }

    private static String buildIdempotencyKey(String eventId, String transactionId) {
        if (eventId != null && !eventId.isBlank()) {
            return "kafka:categorized-tx:event:" + eventId.trim();
        }
        return "kafka:categorized-tx:tx:" + transactionId;
    }

    private static void validate(ProcessTransactionCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("cmd must not be null");

        if (cmd.runId() == null) throw new IllegalArgumentException("runId must not be null");

        if (isBlank(String.valueOf(cmd.transactionId())))
            throw new IllegalArgumentException("transactionId must not be blank");

        if (isBlank(String.valueOf(cmd.userId())))
            throw new IllegalArgumentException("userId must not be blank");

        // categoryId é opcional no seu command (pode ser null)
        // se você quiser exigir em budgeting, valide aqui:
        // if (isBlank(cmd.categoryId())) throw new IllegalArgumentException("categoryId must not be blank");

        if (cmd.amount() == null)
            throw new IllegalArgumentException("amount must not be null");

        if (cmd.amount().signum() < 0)
            throw new IllegalArgumentException("amount must be >= 0");

        if (isBlank(cmd.currency()))
            throw new IllegalArgumentException("currency must not be blank");

        if (cmd.transactionDate() == null)
            throw new IllegalArgumentException("transactionDate must not be null");

        // Valida ISO-4217
        java.util.Currency.getInstance(cmd.currency().trim().toUpperCase());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
