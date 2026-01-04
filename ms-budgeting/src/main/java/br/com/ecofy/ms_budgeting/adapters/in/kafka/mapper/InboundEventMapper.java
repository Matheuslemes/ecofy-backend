package br.com.ecofy.ms_budgeting.adapters.in.kafka.mapper;

import br.com.ecofy.ms_budgeting.adapters.in.kafka.dto.CategorizedTransactionMessage;
import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;
import org.springframework.stereotype.Component;

@Component
public class InboundEventMapper {

    public ProcessTransactionCommand toCommand(CategorizedTransactionMessage msg) {
        return new ProcessTransactionCommand(
                msg.transactionId(),
                msg.userId(),
                msg.categoryId(),
                msg.amount(),
                msg.currency(),
                msg.transactionDate()
        );
    }
}