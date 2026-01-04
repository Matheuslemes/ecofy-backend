package br.com.ecofy.ms_budgeting.core.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProcessTransactionCommand (

        UUID transactionId,
        UUID userId,
        UUID categoryId,
        BigDecimal amount,
        String currency,
        LocalDate transactionDate

) { }
