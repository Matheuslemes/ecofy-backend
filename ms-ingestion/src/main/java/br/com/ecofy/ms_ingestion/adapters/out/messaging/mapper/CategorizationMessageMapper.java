package br.com.ecofy.ms_ingestion.adapters.out.messaging.mapper;

import br.com.ecofy.ms_ingestion.adapters.out.messaging.dto.CategorizationRequestMessage;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;

import java.util.UUID;

public final class CategorizationMessageMapper {

    private CategorizationMessageMapper() {}

    // Mapeia um RawTransaction para a mensagem de request de categorização (payload enviado ao Kafka).
    // userId é o dono do import job, propagado para a listagem por usuário no ms-categorization.
    public static CategorizationRequestMessage from(RawTransaction tx, UUID userId) {
        return new CategorizationRequestMessage(
                tx.id(),
                tx.importJobId(),
                userId,
                tx.description(),
                tx.amount().amount(),
                tx.amount().currency(),
                tx.date().value(),
                tx.sourceType().name()
        );
    }

}
