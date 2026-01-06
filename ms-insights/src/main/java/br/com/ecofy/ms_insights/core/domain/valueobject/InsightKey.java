package br.com.ecofy.ms_insights.core.domain.valueobject;

import br.com.ecofy.ms_insights.core.domain.enums.InsightType;

import java.util.Objects;

public record InsightKey(UserId userId, InsightType type, Period period) {
    public InsightKey {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(period, "period must not be null");
    }

    public String asString() {
        return userId.value() + "|" + type.name() + "|" + period.start() + "|" + period.end() + "|" + period.granularity().name();
    }
}
