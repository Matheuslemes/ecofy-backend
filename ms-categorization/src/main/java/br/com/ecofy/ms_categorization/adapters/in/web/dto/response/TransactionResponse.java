package br.com.ecofy.ms_categorization.adapters.in.web.dto.response;

import br.com.ecofy.ms_categorization.core.domain.Transaction;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(

        UUID id,
        UUID importJobId,
        String description,
        String merchant,
        LocalDate transactionDate,
        long amountCents,
        String currency,
        UUID categoryId,
        String status

) {

    public static TransactionResponse from(Transaction t) {
        boolean uncategorized = t.getCategoryId() == null;
        long amountCents = t.getMoney().getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();

        return new TransactionResponse(
                t.getId(),
                t.getImportJobId(),
                t.getDescription(),
                t.getMerchant().getNormalized(),
                t.getTransactionDate(),
                amountCents,
                t.getMoney().getCurrency().getCurrencyCode(),
                t.getCategoryId(),
                uncategorized ? "UNMATCHED" : "CATEGORIZED"
        );
    }
}
