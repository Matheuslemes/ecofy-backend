package br.com.ecofy.ms_insights.adapters.out.messaging.mapper;

import br.com.ecofy.ms_insights.adapters.out.messaging.dto.InsightCreatedEvent;
import br.com.ecofy.ms_insights.core.domain.Insight;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class EventMapper {

    private EventMapper() {
    }

    public static InsightCreatedEvent toCreatedEvent(Insight insight) {
        return toCreatedEvent(insight, Clock.systemUTC());
    }

    public static InsightCreatedEvent toCreatedEvent(Insight insight, Clock clock) {
        Objects.requireNonNull(insight, "insight must not be null");
        Objects.requireNonNull(clock, "clock must not be null");

        // Mantém o contrato atual, mas evita NPE e padroniza a criação do evento
        String userId = Objects.requireNonNull(insight.getKey(), "insight.key must not be null")
                .userId()
                .value()
                .toString();

        String insightId = Objects.requireNonNull(insight.getId(), "insight.id must not be null").toString();

        Instant occurredAt = Instant.now(clock);

        return new InsightCreatedEvent(
                UUID.randomUUID().toString(),          // eventId
                "insight.created",                      // eventType
                userId,                                 // userId
                insightId,                              // insightId
                insight.getScore(),                     // score
                occurredAt,                              // occurredAt
                insight.getPayload()                    // payload
        );
    }
}
