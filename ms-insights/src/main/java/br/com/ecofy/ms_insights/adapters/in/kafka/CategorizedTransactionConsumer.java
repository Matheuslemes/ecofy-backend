package br.com.ecofy.ms_insights.adapters.in.kafka;

import br.com.ecofy.ms_insights.config.InsightsProperties;
import br.com.ecofy.ms_insights.core.application.service.InsightEventIngestionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class CategorizedTransactionConsumer {

    private final InsightsProperties properties;
    private final ObjectMapper objectMapper;
    private final InsightEventIngestionService ingestionService;

    public CategorizedTransactionConsumer(
            InsightsProperties properties,
            ObjectMapper objectMapper,
            InsightEventIngestionService ingestionService
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.ingestionService = Objects.requireNonNull(ingestionService, "ingestionService must not be null");
    }

    @KafkaListener(
            topics = "${ecofy.insights.topics.categorized-transaction-topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("[CategorizedTransactionConsumer] - [consume] -> empty payload received; skipping");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);

            UUID userId = parseRequiredUuid(root, "userId");
            UUID transactionId = parseOptionalUuid(root, "transactionId");
            UUID categoryId = parseOptionalUuid(root, "categoryId");

            log.info(
                    "[CategorizedTransactionConsumer] - [consume] -> topic={} userId={} transactionId={} categoryId={}",
                    properties.topics().categorizedTransactionTopic(),
                    userId,
                    transactionId,
                    categoryId
            );

            // Mantém o comportamento atual: sinaliza geração por usuário
            ingestionService.onSignalGenerate(userId);

        } catch (Exception ex) {
            // Evita “throws Exception” no listener, mantendo o consumer resiliente e logando o motivo.
            log.error(
                    "[CategorizedTransactionConsumer] - [consume] -> failed to parse/process payload. topic={} payloadSize={} error={}",
                    properties.topics().categorizedTransactionTopic(),
                    payload.length(),
                    ex.getMessage(),
                    ex
            );

            // Se você tiver DLT / error-handler no container, pode apenas relançar RuntimeException.
            // throw new RuntimeException(ex);
        }
    }

    private static UUID parseRequiredUuid(JsonNode root, String field) {
        if (root == null || root.get(field) == null || root.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return UUID.fromString(root.get(field).asText().trim());
    }

    private static UUID parseOptionalUuid(JsonNode root, String field) {
        if (root == null || root.get(field) == null) return null;
        String v = root.get(field).asText();
        if (v == null || v.isBlank()) return null;
        return UUID.fromString(v.trim());
    }
}
