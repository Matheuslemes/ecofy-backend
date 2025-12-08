package br.com.ecofy.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> authEventProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>();

        // Config básica do producer
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Serializer de valor com ObjectMapper do contexto
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper);
        // Não adicionar type info no payload (melhora compatibilidade com outros consumers)
        jsonSerializer.setAddTypeInfo(false);

        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(jsonSerializer);

        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> authEventKafkaTemplate(
            ProducerFactory<String, Object> authEventProducerFactory
    ) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(authEventProducerFactory);
        template.setDefaultTopic("auth.events");
        return template;
    }
}
