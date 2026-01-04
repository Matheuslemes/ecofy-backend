package br.com.ecofy.ms_budgeting.core.domain.valueobject;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {

        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

    }

    public static Money zero(Currency currency) {

        return new Money(BigDecimal.ZERO, currency);

    }

    public Money plus(Money other) {

        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);

    }

    public Money minus(Money other) {

        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);

    }

    public boolean isNegative() {

        return amount.signum() < 0;

    }

    public void requireSameCurrency(Money other) {

        Objects.requireNonNull(other, "other must not be null");

        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }

    }

}