package br.com.ecofy.ms_insights.core.port.out;

import java.time.LocalDate;
import java.util.UUID;

public record CategorizedTxView(
        UUID transactionId,
        UUID userId,
        UUID categoryId,
        long amountCents,
        String currency,
        LocalDate bookingDate,
        boolean income
) {}
