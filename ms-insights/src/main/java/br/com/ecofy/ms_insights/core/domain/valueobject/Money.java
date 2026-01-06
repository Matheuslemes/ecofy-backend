package br.com.ecofy.ms_insights.core.domain.valueobject;

import java.util.Objects;

public record Money(long cents, String currency) {

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
        if (currency.isBlank()) throw new IllegalArgumentException("currency must not be blank");
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(this.cents + other.cents, currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(this.cents - other.cents, currency);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

}
