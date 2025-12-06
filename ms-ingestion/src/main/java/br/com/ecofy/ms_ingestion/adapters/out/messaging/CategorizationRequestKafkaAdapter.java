package br.com.ecofy.ms_ingestion.adapters.out.messaging;

import br.com.ecofy.ms_ingestion.config.KafkaConfig;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.port.out.PublishTransactionForCategorizationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CategorizationRequestKafkaAdapter implements PublishTransactionForCategorizationPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConfig.IngestionTopics topics;

    public CategorizationRequestKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate,
                                             KafkaConfig.IngestionTopics topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void publish(List<RawTransaction> transactions) {
        String topic = topics.getCategorizationRequest();
        log.info("[CategorizationRequestKafkaAdapter] - [publish] -> Enviando {} transações para categorização topic={}",
                transactions.size(), topic);

        transactions.forEach(tx ->
                kafkaTemplate.send(topic, tx.id().toString(), tx)
        );
    }
}