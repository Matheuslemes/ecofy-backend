package br.com.ecofy.ms_ingestion.core.domain.valueobject;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class TransactionDate {

    private final LocalDate value;

    public TransactionDate(LocalDate value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public LocalDate value() {
        return value;
    }

    public ZonedDateTime atStartOfDayUtc() {
        return value.atStartOfDay(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
