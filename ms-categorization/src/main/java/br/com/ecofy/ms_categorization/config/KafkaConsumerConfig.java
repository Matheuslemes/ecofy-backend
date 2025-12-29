package br.com.ecofy.ms_categorization.config;

import br.com.ecofy.ms_categorization.adapters.in.kafka.dto.CategorizationRequestMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, CategorizationRequestMessage> consumerFactory(KafkaProperties props) {
        var cfg = props.buildConsumerProperties();

        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        var valueDeserializer = new JsonDeserializer<>(CategorizationRequestMessage.class);
        valueDeserializer.ignoreTypeHeaders(); // equivalente a USE_TYPE_INFO_HEADERS=false
        valueDeserializer.addTrustedPackages("br.com.ecofy.ms_categorization.adapters.in.kafka.dto");

        return new DefaultKafkaConsumerFactory<>(cfg, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CategorizationRequestMessage> kafkaListenerContainerFactory(
            ConsumerFactory<String, CategorizationRequestMessage> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, CategorizationRequestMessage>();
        factory.setConsumerFactory(consumerFactory);

        var backoff = new ExponentialBackOff(500L, 2.0);
        backoff.setMaxInterval(10_000L);

        factory.setCommonErrorHandler(new DefaultErrorHandler((rec, ex) -> { }, backoff));
        return factory;
    }
}
