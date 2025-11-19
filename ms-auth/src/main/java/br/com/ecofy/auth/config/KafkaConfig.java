package br.com.ecofy.auth.config;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConfig {

    // ProducerFactory especializado para eventos de autenticacao
    // usa json com ObjectMapper compartilhado
    @Bean
    public ProducerFactory<String, Object> authEventProducerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper);
        jsonSerializer.setAddTypeInfo(false);

        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(jsonSerializer);

        return factory;
    }

    // KafkaTemplate que será injetado no AuthEventsKafkaAdapter
    @Bean
    public KafkaTemplate<String, Object> authEventKafkaTemplate(
            ProducerFactory<String, Object> authEventProducerFactory
    ) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(authEventProducerFactory);
        // Exemplo de default topic (pode ser sobrescrito no adapter)
        template.setDefaultTopic("auth.events");
        return template;
    }
}
