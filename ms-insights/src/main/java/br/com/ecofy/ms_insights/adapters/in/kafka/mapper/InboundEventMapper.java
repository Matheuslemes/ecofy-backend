package br.com.ecofy.ms_insights.adapters.in.kafka.mapper;

import br.com.ecofy.ms_insights.adapters.in.kafka.dto.BudgetAlertMessage;
import br.com.ecofy.ms_insights.adapters.in.kafka.dto.CategorizedTransactionMessage;
import br.com.ecofy.ms_insights.core.port.out.CategorizedTxView;

public final class InboundEventMapper {

    private InboundEventMapper() {}

    public static CategorizedTxView toView(CategorizedTransactionMessage m) {
        return new CategorizedTxView(
                m.transactionId(),
                m.userId(),
                m.categoryId(),
                m.amountCents(),
                m.currency(),
                m.bookingDate(),
                m.income()
        );
    }

    public static java.util.UUID userIdFromAlert(BudgetAlertMessage m) {
        return m.userId();
    }
}
