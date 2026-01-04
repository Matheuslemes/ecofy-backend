package br.com.ecofy.ms_budgeting.core.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

public record Period(LocalDate start, LocalDate end) {

    public Period {

        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Period end must be >= start");
        }

    }

    public boolean contains(LocalDate date) {

        return (date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end));

    }

}
