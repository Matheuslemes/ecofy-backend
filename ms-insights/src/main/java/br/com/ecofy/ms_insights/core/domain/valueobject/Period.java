package br.com.ecofy.ms_insights.core.domain.valueobject;

import br.com.ecofy.ms_insights.core.domain.enums.PeriodGranularity;

import java.time.LocalDate;
import java.util.Objects;

public record Period(LocalDate start, LocalDate end, PeriodGranularity granularity) {

    public Period {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        Objects.requireNonNull(granularity, "granularity must not be null");
        if (end.isBefore(start)) throw new IllegalArgumentException("end must be >= start");
    }

}
