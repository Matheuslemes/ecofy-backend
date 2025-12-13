package br.com.ecofy.ms_ingestion.core.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = (currency == null || currency.isBlank())
                ? "BRL"
                : currency.toUpperCase();
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other must not be null");

        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: %s vs %s".formatted(this.currency, other.currency)
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }

    // Factory padrão: usa default BRL (ou o default do construtor)
    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        return new Money(amount, "BRL");
    }

    // Factory opcional: quando você tiver currency do arquivo (ex.: CURDEF)
    public static Money of(BigDecimal amount, String currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        return new Money(amount, currency);
    }
}
