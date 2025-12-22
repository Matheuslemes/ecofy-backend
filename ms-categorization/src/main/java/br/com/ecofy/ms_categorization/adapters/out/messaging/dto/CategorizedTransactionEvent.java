package br.com.ecofy.ms_categorization.adapters.out.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CategorizedTransactionEvent(

        UUID eventId,
        UUID transactionId,
        UUID importJobId,
        String externalId,
        LocalDate transactionDate,
        BigDecimal amount,
        java.util.Currency currency,
        UUID categoryId,
        String mode,
        Instant occurredAt

) { }