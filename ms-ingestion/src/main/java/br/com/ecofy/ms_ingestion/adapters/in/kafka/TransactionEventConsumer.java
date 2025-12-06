package br.com.ecofy.ms_ingestion.adapters.in.kafka;

import br.com.ecofy.ms_ingestion.adapters.out.mapper.TransactionEventMapper;
import br.com.ecofy.ms_ingestion.core.port.in.IngestTransactionEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventConsumer {

    private final IngestTransactionEventUseCase ingestTransactionEventUseCase;
    private final TransactionEventMapper mapper;

    public TransactionEventConsumer(IngestTransactionEventUseCase ingestTransactionEventUseCase,
                                    TransactionEventMapper mapper) {
        this.ingestTransactionEventUseCase = ingestTransactionEventUseCase;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "${ecofy.ingestion.kafka.transaction-event-topic:eco.tx.raw}",
            groupId = "${spring.kafka.consumer.group-id:ms-ingestion}"
    )
    public void consume(ConsumerRecord<String, String> record, @Payload String payload) {
        log.info("[TransactionEventConsumer] - [consume] -> Mensagem recebida key={} topic={} partition={} offset={}",
                record.key(), record.topic(), record.partition(), record.offset());

        var cmd = mapper.toCommand(record, payload);
        ingestTransactionEventUseCase.ingest(cmd);
    }

}