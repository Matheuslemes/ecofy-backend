package br.com.ecofy.ms_budgeting.adapters.in.kafka.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MessageMetadataTest {

    @Test
    @DisplayName("Deve criar metadata com todos os campos preenchidos")
    void shouldCreateMetadataWithAllFields() {
        // Arrange
        String eventId = "event-123";
        Instant occurredAt = Instant.parse("2026-08-11T18:00:00Z");
        String producer = "ms-categorization";
        String traceId = "trace-123";

        // Act
        MessageMetadata metadata = new MessageMetadata(
                eventId,
                occurredAt,
                producer,
                traceId
        );

        // Assert
        assertThat(metadata.eventId()).isEqualTo(eventId);
        assertThat(metadata.occurredAt()).isEqualTo(occurredAt);
        assertThat(metadata.producer()).isEqualTo(producer);
        assertThat(metadata.traceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("Deve permitir criação de metadata com todos os campos nulos")
    void shouldAllowCreationWithAllFieldsNull() {
        // Arrange

        // Act
        MessageMetadata metadata = new MessageMetadata(
                null,
                null,
                null,
                null
        );

        // Assert
        assertThat(metadata.eventId()).isNull();
        assertThat(metadata.occurredAt()).isNull();
        assertThat(metadata.producer()).isNull();
        assertThat(metadata.traceId()).isNull();
    }

    @Test
    @DisplayName("Deve considerar metadata igual quando todos os campos forem iguais")
    void shouldConsiderMetadataEqualWhenAllFieldsAreEqual() {
        // Arrange
        String eventId = "event-123";
        Instant occurredAt = Instant.parse("2026-08-11T18:00:00Z");
        String producer = "ms-categorization";
        String traceId = "trace-123";

        MessageMetadata first = new MessageMetadata(
                eventId,
                occurredAt,
                producer,
                traceId
        );

        MessageMetadata second = new MessageMetadata(
                eventId,
                occurredAt,
                producer,
                traceId
        );

        // Act / Assert
        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
    }

    @Test
    @DisplayName("Deve considerar a própria instância igual a ela mesma")
    void shouldConsiderSameInstanceEqual() {
        // Arrange
        MessageMetadata metadata = new MessageMetadata(
                "event-123",
                Instant.parse("2026-08-11T18:00:00Z"),
                "ms-categorization",
                "trace-123"
        );

        // Act / Assert
        assertThat(metadata).isEqualTo(metadata);
    }

    @Test
    @DisplayName("Deve considerar metadata diferente de nulo e de outro tipo")
    void shouldConsiderMetadataDifferentFromNullAndOtherType() {
        // Arrange
        MessageMetadata metadata = new MessageMetadata(
                "event-123",
                Instant.parse("2026-08-11T18:00:00Z"),
                "ms-categorization",
                "trace-123"
        );

        // Act / Assert
        assertThat(metadata).isNotEqualTo(null);
        assertThat(metadata).isNotEqualTo("metadata");
    }

    @Test
    @DisplayName("Deve considerar metadata diferente quando eventId for diferente")
    void shouldConsiderMetadataDifferentWhenEventIdDiffers() {
        // Arrange
        Instant occurredAt = Instant.parse("2026-08-11T18:00:00Z");

        MessageMetadata first = new MessageMetadata(
                "event-123",
                occurredAt,
                "ms-categorization",
                "trace-123"
        );

        MessageMetadata second = new MessageMetadata(
                "event-456",
                occurredAt,
                "ms-categorization",
                "trace-123"
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar metadata diferente quando occurredAt for diferente")
    void shouldConsiderMetadataDifferentWhenOccurredAtDiffers() {
        // Arrange
        MessageMetadata first = new MessageMetadata(
                "event-123",
                Instant.parse("2026-08-11T18:00:00Z"),
                "ms-categorization",
                "trace-123"
        );

        MessageMetadata second = new MessageMetadata(
                "event-123",
                Instant.parse("2026-08-11T19:00:00Z"),
                "ms-categorization",
                "trace-123"
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar metadata diferente quando producer for diferente")
    void shouldConsiderMetadataDifferentWhenProducerDiffers() {
        // Arrange
        Instant occurredAt = Instant.parse("2026-08-11T18:00:00Z");

        MessageMetadata first = new MessageMetadata(
                "event-123",
                occurredAt,
                "ms-categorization",
                "trace-123"
        );

        MessageMetadata second = new MessageMetadata(
                "event-123",
                occurredAt,
                "ms-budgeting",
                "trace-123"
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar metadata diferente quando traceId for diferente")
    void shouldConsiderMetadataDifferentWhenTraceIdDiffers() {
        // Arrange
        Instant occurredAt = Instant.parse("2026-08-11T18:00:00Z");

        MessageMetadata first = new MessageMetadata(
                "event-123",
                occurredAt,
                "ms-categorization",
                "trace-123"
        );

        MessageMetadata second = new MessageMetadata(
                "event-123",
                occurredAt,
                "ms-categorization",
                "trace-456"
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar iguais duas metadata com todos os campos nulos")
    void shouldConsiderMetadataWithNullFieldsEqual() {
        // Arrange
        MessageMetadata first = new MessageMetadata(
                null,
                null,
                null,
                null
        );

        MessageMetadata second = new MessageMetadata(
                null,
                null,
                null,
                null
        );

        // Act / Assert
        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
    }

    @Test
    @DisplayName("Deve gerar hashCode consistente para a mesma metadata")
    void shouldGenerateConsistentHashCode() {
        // Arrange
        MessageMetadata metadata = new MessageMetadata(
                "event-123",
                Instant.parse("2026-08-11T18:00:00Z"),
                "ms-categorization",
                "trace-123"
        );

        // Act
        int firstHashCode = metadata.hashCode();
        int secondHashCode = metadata.hashCode();

        // Assert
        assertThat(firstHashCode).isEqualTo(secondHashCode);
    }

    @Test
    @DisplayName("Deve gerar representação textual contendo todos os campos")
    void shouldGenerateToStringContainingAllFields() {
        // Arrange
        String eventId = "event-123";
        Instant occurredAt = Instant.parse("2026-08-11T18:00:00Z");
        String producer = "ms-categorization";
        String traceId = "trace-123";

        MessageMetadata metadata = new MessageMetadata(
                eventId,
                occurredAt,
                producer,
                traceId
        );

        // Act
        String result = metadata.toString();

        // Assert
        assertThat(result)
                .contains("MessageMetadata")
                .contains("eventId=" + eventId)
                .contains("occurredAt=" + occurredAt)
                .contains("producer=" + producer)
                .contains("traceId=" + traceId);
    }

    @Test
    @DisplayName("Deve representar campos nulos no toString")
    void shouldRepresentNullFieldsInToString() {
        // Arrange
        MessageMetadata metadata = new MessageMetadata(
                null,
                null,
                null,
                null
        );

        // Act
        String result = metadata.toString();

        // Assert
        assertThat(result)
                .contains("eventId=null")
                .contains("occurredAt=null")
                .contains("producer=null")
                .contains("traceId=null");
    }
}
