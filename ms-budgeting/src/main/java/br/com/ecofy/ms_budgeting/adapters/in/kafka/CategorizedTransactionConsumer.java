package br.com.ecofy.ms_budgeting.adapters.in.kafka;

import br.com.ecofy.ms_budgeting.adapters.in.kafka.dto.CategorizedTransactionMessage;
import br.com.ecofy.ms_budgeting.adapters.in.kafka.mapper.InboundEventMapper;
import br.com.ecofy.ms_budgeting.core.port.in.ProcessTransactionForBudgetUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategorizedTransactionConsumer {

    private final InboundEventMapper mapper;
    private final ProcessTransactionForBudgetUseCase useCase;

    @KafkaListener(
            topics = "${ecofy.budgeting.topics.categorized-transaction}",
            groupId = "${spring.application.name}"
    )
    public void onMessage(CategorizedTransactionMessage msg) {
        log.debug("[CategorizedTransactionConsumer] - [onMessage] -> txId={} userId={} categoryId={}",
                msg.transactionId(), msg.userId(), msg.categoryId());

        useCase.process(mapper.toCommand(msg));
    }

}