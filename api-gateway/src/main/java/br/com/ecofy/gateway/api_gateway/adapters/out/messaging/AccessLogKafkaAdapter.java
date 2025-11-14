package br.com.ecofy.gateway.api_gateway.adapters.out.messaging;

import br.com.ecofy.gateway.api_gateway.core.port.out.PublishAccessLogPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccessLogKafkaAdapter implements PublishAccessLogPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String topicName = "gateway.access.log";

    public AccessLogKafkaAdapter(KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishAccessLog(PublishAccessLogPort.AccessLogEntry entry) {
        String key = entry.path();
        String payload = toJson(entry);

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, payload);
        if (entry.tenantContext() != null) {
            record.headers().add("x-tenant-id", entry.tenantContext().tenantId().getBytes());
            record.headers().add("x-user-id", entry.tenantContext().subject().getBytes());
        }
        record.headers().add("x-trace-id",
                String.valueOf(entry.extraTags().getOrDefault("traceId", "")).getBytes());

        kafkaTemplate.send(record);
    }

    private String toJson(AccessLogEntry entry) {
        try {
            return objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            return "{ \"error\": \"failed-to-serialize-access-log\", \"path\": \"" + entry.path() + "\" }";
        }
    }
}