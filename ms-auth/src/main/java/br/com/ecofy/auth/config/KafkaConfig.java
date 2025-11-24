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

/**
 * Configuração de Kafka específica para os eventos de autenticação.
 */
@Configuration
public class KafkaConfig {

    /**
     * ProducerFactory especializado para eventos de autenticação.
     * Usa JsonSerializer com ObjectMapper do contexto.
     */
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

    /**
     * KafkaTemplate que será injetado no adapter de eventos (AuthEventsKafkaAdapter).
     */
    @Bean
    public KafkaTemplate<String, Object> authEventKafkaTemplate(
            ProducerFactory<String, Object> authEventProducerFactory
    ) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(authEventProducerFactory);
        // Pode sobrescrever o tópico no adapter, aqui é só um default
        template.setDefaultTopic("auth.events");
        return template;
    }
}
