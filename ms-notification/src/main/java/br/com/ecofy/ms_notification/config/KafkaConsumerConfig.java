package br.com.ecofy.ms_notification.config;

import br.com.ecofy.notification.adapters.in.kafka.dto.BudgetAlertEventMessage;
import br.com.ecofy.notification.adapters.in.kafka.dto.InsightCreatedEventMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    ConsumerFactory<String, BudgetAlertEventMessage> budgetAlertConsumerFactory() {
        return buildJsonConsumerFactory(BudgetAlertEventMessage.class);
    }

    @Bean
    ConsumerFactory<String, InsightCreatedEventMessage> insightCreatedConsumerFactory() {
        return buildJsonConsumerFactory(InsightCreatedEventMessage.class);
    }

    private <T> ConsumerFactory<String, T> buildJsonConsumerFactory(Class<T> type) {

        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.putIfAbsent(JsonDeserializer.TRUSTED_PACKAGES, "br.com.ecofy.notification.adapters.in.kafka.dto");
        props.putIfAbsent(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(type, false);
        valueDeserializer.addTrustedPackages("br.com.ecofy.notification.adapters.in.kafka.dto");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, BudgetAlertEventMessage> budgetAlertKafkaListenerContainerFactory(
            ConsumerFactory<String, BudgetAlertEventMessage> budgetAlertConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, BudgetAlertEventMessage>();
        factory.setConsumerFactory(budgetAlertConsumerFactory);
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, InsightCreatedEventMessage> insightCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, InsightCreatedEventMessage> insightCreatedConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, InsightCreatedEventMessage>();
        factory.setConsumerFactory(insightCreatedConsumerFactory);
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);
        return factory;
    }
}