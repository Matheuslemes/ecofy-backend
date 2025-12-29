package br.com.ecofy.ms_categorization.adapters.out.messaging.mapper;

import br.com.ecofy.ms_categorization.adapters.out.messaging.dto.CategorizationAppliedEvent;
import br.com.ecofy.ms_categorization.adapters.out.messaging.dto.CategorizedTransactionEvent;
import br.com.ecofy.ms_categorization.core.domain.CategorizationSuggestion;
import br.com.ecofy.ms_categorization.core.domain.Transaction;
import br.com.ecofy.ms_categorization.core.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Component
public class EventMapper {

    private final Clock clock;

    public EventMapper() {
        this(Clock.systemUTC());
    }

    public EventMapper(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public CategorizedTransactionEvent toCategorizedEvent(Transaction tx, UUID categoryId, String mode) {
        return toCategorizedEvent(tx, categoryId, mode, Instant.now(clock), UUID.randomUUID());
    }

    public CategorizedTransactionEvent toCategorizedEvent(Transaction tx, UUID categoryId, String mode, Instant occurredAt) {
        return toCategorizedEvent(tx, categoryId, mode, occurredAt, UUID.randomUUID());
    }

    public CategorizedTransactionEvent toCategorizedEvent(
            Transaction tx,
            UUID categoryId,
            String mode,
            Instant occurredAt,
            UUID eventId
    ) {
        Objects.requireNonNull(tx, "tx must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");

        Money money = Objects.requireNonNull(tx.getMoney(), "tx.money must not be null");

        return new CategorizedTransactionEvent(
                eventId,
                tx.getId(),
                tx.getImportJobId(),
                tx.getExternalId(),
                tx.getTransactionDate(),
                money.getAmount(),
                money.getCurrency(),
                categoryId,
                mode,
                occurredAt
        );
    }

    public CategorizationAppliedEvent toAppliedEvent(
            UUID txId,
            UUID categoryId,
            UUID ruleId,
            String mode,
            int score,
            UUID suggestionId
    ) {
        return toAppliedEvent(txId, categoryId, ruleId, mode, score, suggestionId, Instant.now(clock), UUID.randomUUID());
    }

    public CategorizationAppliedEvent toAppliedEvent(
            UUID txId,
            UUID categoryId,
            UUID ruleId,
            String mode,
            int score,
            UUID suggestionId,
            Instant occurredAt
    ) {
        return toAppliedEvent(txId, categoryId, ruleId, mode, score, suggestionId, occurredAt, UUID.randomUUID());
    }

    public CategorizationAppliedEvent toAppliedEvent(
            UUID txId,
            UUID categoryId,
            UUID ruleId,
            String mode,
            int score,
            UUID suggestionId,
            Instant occurredAt,
            UUID eventId
    ) {
        Objects.requireNonNull(txId, "txId must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");

        return new CategorizationAppliedEvent(
                eventId,
                txId,
                categoryId,
                ruleId,
                mode,
                score,
                suggestionId,
                occurredAt
        );
    }

    public CategorizationAppliedEvent toAppliedEvent(CategorizationSuggestion suggestion, String mode) {
        return toAppliedEvent(suggestion, mode, Instant.now(clock), UUID.randomUUID());
    }

    public CategorizationAppliedEvent toAppliedEvent(CategorizationSuggestion suggestion, String mode, Instant occurredAt) {
        return toAppliedEvent(suggestion, mode, occurredAt, UUID.randomUUID());
    }

    public CategorizationAppliedEvent toAppliedEvent(
            CategorizationSuggestion suggestion,
            String mode,
            Instant occurredAt,
            UUID eventId
    ) {
        Objects.requireNonNull(suggestion, "suggestion must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");

        return new CategorizationAppliedEvent(
                eventId,
                suggestion.getTransactionId(),
                suggestion.getCategoryId(),
                suggestion.getRuleId(),
                mode,
                suggestion.getScore(),
                suggestion.getId(),
                occurredAt
        );
    }

    private static String currencyCode(Currency currency) {
        return currency == null ? null : currency.getCurrencyCode();
    }
}
