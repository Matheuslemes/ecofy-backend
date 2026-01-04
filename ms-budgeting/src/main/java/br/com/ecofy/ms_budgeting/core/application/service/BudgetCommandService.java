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

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class BudgetCommandService implements CreateBudgetUseCase, UpdateBudgetUseCase, DeleteBudgetUseCase {

    private final SaveBudgetPort saveBudgetPort;
    private final LoadBudgetsPort loadBudgetsPort;
    private final DeleteBudgetPort deleteBudgetPort;
    private final IdempotencyPort idempotencyPort;
    private final BudgetingProperties props;

    public BudgetCommandService(SaveBudgetPort saveBudgetPort,
                                LoadBudgetsPort loadBudgetsPort,
                                DeleteBudgetPort deleteBudgetPort,
                                IdempotencyPort idempotencyPort,
                                BudgetingProperties props) {
        this.saveBudgetPort = Objects.requireNonNull(saveBudgetPort);
        this.loadBudgetsPort = Objects.requireNonNull(loadBudgetsPort);
        this.deleteBudgetPort = Objects.requireNonNull(deleteBudgetPort);
        this.idempotencyPort = Objects.requireNonNull(idempotencyPort);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public BudgetResult create(CreateBudgetCommand cmd, String idempotencyKey) {
        requireIdempotency(idempotencyKey, "api:createBudget");

        Instant now = Instant.now();

        var key = new BudgetKey(
                new UserId(cmd.userId()),
                new CategoryId(cmd.categoryId()),
                new Period(cmd.periodStart(), cmd.periodEnd())
        );

        String naturalKey = key.asNaturalKey();
        if (loadBudgetsPort.existsByNaturalKey(naturalKey)) {
            throw new BudgetAlreadyExistsException(naturalKey);
        }

        var budget = new Budget(
                UUID.randomUUID(),
                key,
                cmd.periodType(),
                new Money(cmd.limitAmount(), Currency.getInstance(cmd.currency())),
                cmd.status() == null ? BudgetStatus.ACTIVE : cmd.status(),
                now,
                now
        );

        Budget saved = saveBudgetPort.save(budget);

        log.info("[BudgetCommandService] - [create] -> SUCCESS budgetId={} userId={} categoryId={} key={}",
                saved.getId(), cmd.userId(), cmd.categoryId(), naturalKey);

        return toResult(saved);
    }

    @Override
    public BudgetResult update(UpdateBudgetCommand cmd, String idempotencyKey) {
        requireIdempotency(idempotencyKey, "api:updateBudget");

        Budget budget = loadBudgetsPort.findById(cmd.budgetId())
                .orElseThrow(() -> new BudgetNotFoundException(cmd.budgetId()));

        Instant now = Instant.now();

        if (cmd.newLimitAmount() != null && cmd.currency() != null) {
            budget.updateLimit(new Money(cmd.newLimitAmount(), Currency.getInstance(cmd.currency())), now);
        }

        if (cmd.status() != null) {
            budget.updateStatus(cmd.status(), now);
        }

        Budget saved = saveBudgetPort.save(budget);

        log.info("[BudgetCommandService] - [update] -> SUCCESS budgetId={} status={} updatedAt={}",
                saved.getId(), saved.getStatus(), saved.getUpdatedAt());

        return toResult(saved);
    }

    @Override
    public void delete(DeleteBudgetCommand cmd, String idempotencyKey) {
        requireIdempotency(idempotencyKey, "api:deleteBudget");

        if (!deleteBudgetPort.existsById(cmd.budgetId())) {
            throw new BudgetNotFoundException(cmd.budgetId());
        }

        deleteBudgetPort.deleteById(cmd.budgetId());
        log.info("[BudgetCommandService] - [delete] -> SUCCESS budgetId={}", cmd.budgetId());
    }

    private void requireIdempotency(String key, String scope) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header must be provided");
        }
        boolean acquired = idempotencyPort.tryAcquire(key, props.idempotency().ttl(), scope);
        if (!acquired) throw new IdempotencyViolationException(key);
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