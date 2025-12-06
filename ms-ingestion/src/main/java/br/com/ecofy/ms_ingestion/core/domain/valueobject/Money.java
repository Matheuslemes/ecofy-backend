package br.com.ecofy.ms_ingestion.core.domain.valueobject;


import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = Objects.requireNonNullElse(currency, Currency.getInstance("BRL"));
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount;
    }

}