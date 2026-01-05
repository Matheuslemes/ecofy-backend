package br.com.ecofy.ms_budgeting.core.application.service;

import br.com.ecofy.ms_budgeting.config.BudgetingProperties;
import br.com.ecofy.ms_budgeting.core.application.command.CreateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.command.DeleteBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.command.UpdateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;
import br.com.ecofy.ms_budgeting.core.domain.Budget;
import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetAlreadyExistsException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetNotFoundException;
import br.com.ecofy.ms_budgeting.core.domain.exception.IdempotencyViolationException;
import br.com.ecofy.ms_budgeting.core.domain.valueobject.*;
import br.com.ecofy.ms_budgeting.core.port.in.CreateBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.DeleteBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.UpdateBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.out.DeleteBudgetPort;
import br.com.ecofy.ms_budgeting.core.port.out.IdempotencyPort;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetsPort;
import br.com.ecofy.ms_budgeting.core.port.out.SaveBudgetPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class BudgetCommandService implements CreateBudgetUseCase, UpdateBudgetUseCase, DeleteBudgetUseCase {

    private static final String SCOPE_CREATE = "api:budget:create";
    private static final String SCOPE_UPDATE = "api:budget:update";
    private static final String SCOPE_DELETE = "api:budget:delete";

    private final SaveBudgetPort saveBudgetPort;
    private final LoadBudgetsPort loadBudgetsPort;
    private final DeleteBudgetPort deleteBudgetPort;
    private final IdempotencyPort idempotencyPort;
    private final BudgetingProperties props;
    private final Clock clock;

    public BudgetCommandService(
            SaveBudgetPort saveBudgetPort,
            LoadBudgetsPort loadBudgetsPort,
            DeleteBudgetPort deleteBudgetPort,
            IdempotencyPort idempotencyPort,
            BudgetingProperties props,
            Clock clock
    ) {
        this.saveBudgetPort = Objects.requireNonNull(saveBudgetPort, "saveBudgetPort must not be null");
        this.loadBudgetsPort = Objects.requireNonNull(loadBudgetsPort, "loadBudgetsPort must not be null");
        this.deleteBudgetPort = Objects.requireNonNull(deleteBudgetPort, "deleteBudgetPort must not be null");
        this.idempotencyPort = Objects.requireNonNull(idempotencyPort, "idempotencyPort must not be null");
        this.props = Objects.requireNonNull(props, "props must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    @Transactional
    public BudgetResult create(CreateBudgetCommand cmd, String idempotencyKey) {
        Objects.requireNonNull(cmd, "cmd must not be null");
        acquireIdempotencyOrThrow(idempotencyKey, SCOPE_CREATE);

        validateCreateCommand(cmd);

        Instant now = Instant.now(clock);

        var key = new BudgetKey(
                new UserId(cmd.userId()),
                new CategoryId(cmd.categoryId()),
                new Period(cmd.periodStart(), cmd.periodEnd())
        );

        String naturalKey = key.asNaturalKey();
        if (loadBudgetsPort.existsByNaturalKey(naturalKey)) {
            throw new BudgetAlreadyExistsException(naturalKey);
        }

        Currency currency = parseCurrency(cmd.currency());
        var limit = new Money(cmd.limitAmount(), currency);

        var budget = new Budget(
                UUID.randomUUID(),
                key,
                cmd.periodType(),
                limit,
                cmd.status() != null ? cmd.status() : BudgetStatus.ACTIVE,
                now,
                now
        );

        Budget saved = saveBudgetPort.save(budget);

        log.info(
                "[BudgetCommandService] - [create] -> SUCCESS budgetId={} userId={} categoryId={} naturalKey={}",
                saved.getId(), saved.getKey().userId().value(), saved.getKey().categoryId().value(), naturalKey
        );

        return toResult(saved);
    }

    @Override
    @Transactional
    public BudgetResult update(UpdateBudgetCommand cmd, String idempotencyKey) {
        Objects.requireNonNull(cmd, "cmd must not be null");
        acquireIdempotencyOrThrow(idempotencyKey, SCOPE_UPDATE);

        UUID budgetId = requireNonNull(cmd.budgetId(), "budgetId");
        Budget budget = loadBudgetsPort.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));

        Instant now = Instant.now(clock);

        boolean changed = false;

        // Update limit:
        // - Se vier amount e NÃO vier currency, usa currency atual.
        // - Se vier currency e NÃO vier amount, rejeita (evita update parcial confuso).
        if (cmd.newLimitAmount() != null || cmd.currency() != null) {
            if (cmd.newLimitAmount() == null) {
                throw new IllegalArgumentException("newLimitAmount must be provided when currency is provided");
            }
            Currency currency = cmd.currency() != null
                    ? parseCurrency(cmd.currency())
                    : budget.getLimit().currency();

            budget.updateLimit(new Money(cmd.newLimitAmount(), currency), now);
            changed = true;
        }

        if (cmd.status() != null) {
            budget.updateStatus(cmd.status(), now);
            changed = true;
        }

        if (!changed) {
            log.info("[BudgetCommandService] - [update] -> NOOP budgetId={} (no changes)", budgetId);
            return toResult(budget);
        }

        Budget saved = saveBudgetPort.save(budget);

        log.info(
                "[BudgetCommandService] - [update] -> SUCCESS budgetId={} status={} updatedAt={}",
                saved.getId(), saved.getStatus(), saved.getUpdatedAt()
        );

        return toResult(saved);
    }

    @Override
    @Transactional
    public void delete(DeleteBudgetCommand cmd, String idempotencyKey) {
        Objects.requireNonNull(cmd, "cmd must not be null");
        acquireIdempotencyOrThrow(idempotencyKey, SCOPE_DELETE);

        UUID budgetId = requireNonNull(cmd.budgetId(), "budgetId");

        if (!deleteBudgetPort.existsById(budgetId)) {
            throw new BudgetNotFoundException(budgetId);
        }

        deleteBudgetPort.deleteById(budgetId);

        log.info("[BudgetCommandService] - [delete] -> SUCCESS budgetId={}", budgetId);
    }

    private void acquireIdempotencyOrThrow(String key, String scope) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header must be provided");
        }

        boolean acquired = idempotencyPort.tryAcquire(
                key.trim(),
                props.idempotency().ttl(),
                scope
        );

        if (!acquired) {
            throw new IdempotencyViolationException(key);
        }
    }

    private void validateCreateCommand(CreateBudgetCommand cmd) {
        requireNonNull(cmd.userId(), "userId");
        requireNonNull(cmd.categoryId(), "categoryId");
        requireNonNull(cmd.periodType(), "periodType");
        requireNonNull(cmd.periodStart(), "periodStart");
        requireNonNull(cmd.periodEnd(), "periodEnd");
        requireNonNull(cmd.limitAmount(), "limitAmount");

        if (cmd.periodStart().isAfter(cmd.periodEnd())) {
            throw new IllegalArgumentException("periodStart must be <= periodEnd");
        }

        // Currency é string no command; valida aqui com mensagem amigável
        parseCurrency(cmd.currency());
    }

    private static Currency parseCurrency(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
        try {
            return Currency.getInstance(code.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid currency code: " + code, ex);
        }
    }

    private static <T> T requireNonNull(T v, String field) {
        return Objects.requireNonNull(v, field + " must not be null");
    }

    private static BudgetResult toResult(Budget b) {
        return new BudgetResult(
                b.getId(),
                b.getKey().userId().value(),
                b.getKey().categoryId().value(),
                b.getPeriodType(),
                b.getKey().period().start(),
                b.getKey().period().end(),
                b.getLimit().amount(),
                b.getLimit().currency().getCurrencyCode(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}
