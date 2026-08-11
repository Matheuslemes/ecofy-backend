package br.com.ecofy.ms_budgeting.adapters.in.kafka.mapper;

import br.com.ecofy.ms_budgeting.adapters.in.kafka.dto.CategorizedTransactionMessage;
import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InboundEventMapperTest {

    private final InboundEventMapper mapper = new InboundEventMapper();

    @Test
    @DisplayName("Deve converter mensagem em comando utilizando os headers do Kafka")
    void shouldConvertMessageToCommandUsingKafkaHeaders() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        2,
                        42L,
                        "transaction-key",
                        message
                );

        record.headers().add(
                "event_id",
                "event-123".getBytes(StandardCharsets.UTF_8)
        );
        record.headers().add(
                "correlation_id",
                "correlation-456".getBytes(StandardCharsets.UTF_8)
        );

        // Act
        ProcessTransactionCommand result = mapper.toCommand(message, record);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.runId()).isNotNull();
        assertThat(result.transactionId()).isEqualTo(message.transactionId());
        assertThat(result.userId()).isEqualTo(message.userId());
        assertThat(result.categoryId()).isEqualTo(message.categoryId());
        assertThat(result.amount()).isEqualByComparingTo(message.amount());
        assertThat(result.currency()).isEqualTo(message.currency());
        assertThat(result.transactionDate()).isEqualTo(message.transactionDate());

        assertThat(result.metadata().eventId()).isEqualTo("event-123");
        assertThat(result.metadata().correlationId()).isEqualTo("correlation-456");
        assertThat(result.metadata().topic()).isEqualTo(record.topic());
        assertThat(result.metadata().partition()).isEqualTo(record.partition());
        assertThat(result.metadata().offset()).isEqualTo(record.offset());
        assertThat(result.metadata().key()).isEqualTo(record.key());
        assertThat(result.metadata())
                .isEqualTo(new ProcessTransactionCommand.EventMetadata(
                        "event-123",
                        "correlation-456",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        Instant.ofEpochMilli(record.timestamp())
                ));
    }

    @Test
    @DisplayName("Deve utilizar o último header quando houver identificadores duplicados")
    void shouldUseLastHeaderWhenHeadersAreDuplicated() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        10L,
                        "key",
                        message
                );

        record.headers()
                .add("event_id", "event-antigo".getBytes(StandardCharsets.UTF_8))
                .add("event_id", "event-novo".getBytes(StandardCharsets.UTF_8))
                .add("correlation_id", "correlation-antigo".getBytes(StandardCharsets.UTF_8))
                .add("correlation_id", "correlation-novo".getBytes(StandardCharsets.UTF_8));

        // Act
        ProcessTransactionCommand result = mapper.toCommand(message, record);

        // Assert
        assertThat(result.metadata().eventId()).isEqualTo("event-novo");
        assertThat(result.metadata().correlationId()).isEqualTo("correlation-novo");
    }

    @Test
    @DisplayName("Deve converter headers em branco para nulo")
    void shouldConvertBlankHeadersToNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        1L,
                        "key",
                        message
                );

        record.headers()
                .add("event_id", "   ".getBytes(StandardCharsets.UTF_8))
                .add("correlation_id", "\t".getBytes(StandardCharsets.UTF_8));

        // Act
        ProcessTransactionCommand result = mapper.toCommand(message, record);

        // Assert
        assertThat(result.metadata().eventId()).isNull();
        assertThat(result.metadata().correlationId()).isNull();
    }

    @Test
    @DisplayName("Deve retornar identificadores nulos quando os headers não existirem")
    void shouldReturnNullIdentifiersWhenHeadersDoNotExist() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        1L,
                        "key",
                        message
                );

        // Act
        ProcessTransactionCommand result = mapper.toCommand(message, record);

        // Assert
        assertThat(result.metadata().eventId()).isNull();
        assertThat(result.metadata().correlationId()).isNull();
    }

    @Test
    @DisplayName("Deve retornar identificador nulo quando o valor do header for nulo")
    void shouldReturnNullWhenHeaderValueIsNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, CategorizedTransactionMessage> record =
                mock(ConsumerRecord.class);

        RecordHeaders headers = new RecordHeaders();

        Header eventHeader = mock(Header.class);
        when(eventHeader.key()).thenReturn("event_id");
        when(eventHeader.value()).thenReturn(null);

        headers.add(eventHeader);
        headers.add(
                "correlation_id",
                "correlation-123".getBytes(StandardCharsets.UTF_8)
        );

        when(record.headers()).thenReturn(headers);
        when(record.topic()).thenReturn("categorized-transactions");
        when(record.partition()).thenReturn(1);
        when(record.offset()).thenReturn(10L);
        when(record.key()).thenReturn("key");
        when(record.timestamp()).thenReturn(1_000L);

        // Act
        ProcessTransactionCommand result = mapper.toCommand(message, record);

        // Assert
        assertThat(result.metadata().eventId()).isNull();
        assertThat(result.metadata().correlationId()).isEqualTo("correlation-123");
    }

    @Test
    @DisplayName("Deve tratar headers nulos como identificadores ausentes")
    void shouldTreatNullHeadersAsMissingIdentifiers() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        @SuppressWarnings("unchecked")
        ConsumerRecord<String, CategorizedTransactionMessage> record =
                mock(ConsumerRecord.class);

        when(record.headers()).thenReturn(null);
        when(record.topic()).thenReturn("categorized-transactions");
        when(record.partition()).thenReturn(1);
        when(record.offset()).thenReturn(20L);
        when(record.key()).thenReturn("key");
        when(record.timestamp()).thenReturn(2_000L);

        // Act
        ProcessTransactionCommand result = mapper.toCommand(message, record);

        // Assert
        assertThat(result.metadata().eventId()).isNull();
        assertThat(result.metadata().correlationId()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando o registro for nulo na conversão automática")
    void shouldThrowExceptionWhenRecordIsNullInAutomaticConversion() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        // Act / Assert
        assertThatThrownBy(() -> mapper.toCommand(message, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("record must not be null");
    }

    @Test
    @DisplayName("Deve converter mensagem utilizando identificadores e metadados informados")
    void shouldConvertMessageUsingProvidedIdentifiersAndMetadata() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();
        UUID runId = UUID.randomUUID();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        3,
                        99L,
                        "transaction-key",
                        message
                );

        String eventId = "event-123";
        String correlationId = "correlation-456";

        // Act
        ProcessTransactionCommand result = mapper.toCommand(
                message,
                runId,
                eventId,
                correlationId,
                record
        );

        // Assert
        ProcessTransactionCommand expected = new ProcessTransactionCommand(
                runId,
                message.transactionId(),
                message.userId(),
                message.categoryId(),
                message.amount(),
                message.currency(),
                message.transactionDate(),
                new ProcessTransactionCommand.EventMetadata(
                        eventId,
                        correlationId,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        Instant.ofEpochMilli(record.timestamp())
                )
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve remover espaços dos identificadores informados")
    void shouldTrimProvidedIdentifiers() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();
        UUID runId = UUID.randomUUID();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        1,
                        15L,
                        "key",
                        message
                );

        // Act
        ProcessTransactionCommand result = mapper.toCommand(
                message,
                runId,
                "  event-123  ",
                "  correlation-456  ",
                record
        );

        // Assert
        assertThat(result.metadata().eventId()).isEqualTo("event-123");
        assertThat(result.metadata().correlationId()).isEqualTo("correlation-456");
    }

    @Test
    @DisplayName("Deve converter identificadores nulos para nulo")
    void shouldKeepNullIdentifiersAsNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();
        UUID runId = UUID.randomUUID();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        1L,
                        null,
                        message
                );

        // Act
        ProcessTransactionCommand result = mapper.toCommand(
                message,
                runId,
                null,
                null,
                record
        );

        // Assert
        assertThat(result.metadata().eventId()).isNull();
        assertThat(result.metadata().correlationId()).isNull();
    }

    @Test
    @DisplayName("Deve converter identificadores vazios ou em branco para nulo")
    void shouldConvertBlankProvidedIdentifiersToNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();
        UUID runId = UUID.randomUUID();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        1L,
                        "key",
                        message
                );

        // Act
        ProcessTransactionCommand result = mapper.toCommand(
                message,
                runId,
                "",
                "   ",
                record
        );

        // Assert
        assertThat(result.metadata().eventId()).isNull();
        assertThat(result.metadata().correlationId()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando a mensagem for nula")
    void shouldThrowExceptionWhenMessageIsNull() {
        // Arrange
        UUID runId = UUID.randomUUID();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        1L,
                        "key",
                        null
                );

        // Act / Assert
        assertThatThrownBy(() -> mapper.toCommand(
                null,
                runId,
                "event-123",
                "correlation-123",
                record
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("msg must not be null");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o runId for nulo")
    void shouldThrowExceptionWhenRunIdIsNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                new ConsumerRecord<>(
                        "categorized-transactions",
                        0,
                        1L,
                        "key",
                        message
                );

        // Act / Assert
        assertThatThrownBy(() -> mapper.toCommand(
                message,
                null,
                "event-123",
                "correlation-123",
                record
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("runId must not be null");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o registro for nulo")
    void shouldThrowExceptionWhenRecordIsNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();
        UUID runId = UUID.randomUUID();

        // Act / Assert
        assertThatThrownBy(() -> mapper.toCommand(
                message,
                runId,
                "event-123",
                "correlation-123",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("record must not be null");
    }

    private CategorizedTransactionMessage createMessage() {
        return new CategorizedTransactionMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150.75"),
                "BRL",
                LocalDate.of(2026, 8, 11),
                null
        );
    }
}
