package br.com.ecofy.ms_categorization.core.application.command;

import br.com.ecofy.ms_categorization.core.domain.Transaction;

import java.util.Objects;

public record AutoCategorizeCommand(

        String idempotencyKey,
        Transaction inboundTransaction

) {

    public AutoCategorizeCommand {

        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        Objects.requireNonNull(inboundTransaction, "inboundTransaction must not be null");

        if (idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey must not be blank");

    }

}