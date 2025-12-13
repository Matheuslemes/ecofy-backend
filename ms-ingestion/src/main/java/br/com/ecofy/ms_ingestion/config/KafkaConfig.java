package br.com.ecofy.ms_ingestion.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    @ConfigurationProperties("ecofy.ingestion.kafka.topics")
    public IngestionTopics ingestionTopics() {
        return new IngestionTopics();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonSerializer.class);

        log.info("[KafkaConfig] - [producerFactory] -> Producer configurado com bootstrapServers={}",
                kafkaProperties.getBootstrapServers());

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        log.info("[KafkaConfig] - [consumerFactory] -> Consumer configurado com bootstrapServers={}",
                kafkaProperties.getBootstrapServers());

        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Tópicos principais (ajuste nomes se quiser)
    @Bean
    public NewTopic ingestionTransactionImportedTopic(IngestionTopics topics) {
        return TopicBuilder.name(topics.getTransactionImported())
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ingestionCategorizationRequestTopic(IngestionTopics topics) {
        return TopicBuilder.name(topics.getCategorizationRequest())
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Getter
    @Setter
    public static class IngestionTopics {

        private String transactionImported = "eco.ingestion.transaction.imported";

        private String importJobStatusChanged = "eco.ingestion.import-job.status-changed";

        private String categorizationRequest = "eco.categorization.request";
    }


}
