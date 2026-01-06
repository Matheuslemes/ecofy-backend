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
public class BudgetAlertConsumer {

    private final InsightsProperties properties;
    private final ObjectMapper objectMapper;
    private final InsightEventIngestionService ingestionService;

    public BudgetAlertConsumer(
            InsightsProperties properties,
            ObjectMapper objectMapper,
            InsightEventIngestionService ingestionService
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.ingestionService = Objects.requireNonNull(ingestionService, "ingestionService must not be null");
    }

    /**
     * Preferir placeholder de property ao invés de SpEL por nome de bean.
     * Isso evita o erro: "bean named 'insightsProperties' could not be found".
     *
     * Requer no application.yml:
     * ecofy.insights.topics.budget-alert-topic: eco.budget.alert
     */
    @KafkaListener(
            topics = "${ecofy.insights.topics.budget-alert-topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("[BudgetAlertConsumer] - [consume] -> empty payload received; skipping");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);

            UUID userId = parseRequiredUuid(root, "userId");
            UUID budgetId = parseOptionalUuid(root, "budgetId");
            String severity = parseOptionalText(root, "severity");
            String status = parseOptionalText(root, "status");

            log.info(
                    "[BudgetAlertConsumer] - [consume] -> userId={} budgetId={} severity={} status={}",
                    userId,
                    budgetId,
                    severity,
                    status
            );

            // Mantém o comportamento atual: sinaliza geração por usuário
            ingestionService.onSignalGenerate(userId);

        } catch (Exception ex) {
            log.error(
                    "[BudgetAlertConsumer] - [consume] -> failed to parse/process payload. payloadSize={} error={}",
                    payload.length(),
                    ex.getMessage(),
                    ex
            );

            // Se você estiver usando DLT/DefaultErrorHandler e quiser reprocessar/falhar a partição, relance:
            // throw new RuntimeException(ex);
        }
    }

    private static UUID parseRequiredUuid(JsonNode root, String field) {
        if (root == null || root.get(field) == null) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        String v = root.get(field).asText(null);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return UUID.fromString(v.trim());
    }

    private static UUID parseOptionalUuid(JsonNode root, String field) {
        if (root == null || root.get(field) == null) return null;
        String v = root.get(field).asText(null);
        if (v == null || v.isBlank()) return null;
        return UUID.fromString(v.trim());
    }

    private static String parseOptionalText(JsonNode root, String field) {
        if (root == null || root.get(field) == null) return null;
        String v = root.get(field).asText(null);
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
