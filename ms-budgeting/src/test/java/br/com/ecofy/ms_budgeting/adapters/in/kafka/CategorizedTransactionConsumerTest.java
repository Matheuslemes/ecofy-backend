package br.com.ecofy.ms_budgeting.adapters.in.kafka;

import br.com.ecofy.ms_budgeting.adapters.in.kafka.dto.CategorizedTransactionMessage;
import br.com.ecofy.ms_budgeting.adapters.in.kafka.mapper.InboundEventMapper;
import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;
import br.com.ecofy.ms_budgeting.core.application.service.BudgetEventIngestionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategorizedTransactionConsumerTest {

    @Mock
    private InboundEventMapper mapper;

    @Mock
    private BudgetEventIngestionService ingestionService;

    @InjectMocks
    private CategorizedTransactionConsumer consumer;

    @Test
    @DisplayName("Deve converter e processar mensagem válida com headers de correlação e evento")
    void shouldConvertAndProcessValidMessageWithHeaders() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        record.headers()
                .add(
                        "correlationId",
                        "correlation-123".getBytes(StandardCharsets.UTF_8)
                )
                .add(
                        "eventId",
                        "event-456".getBytes(StandardCharsets.UTF_8)
                );

        ProcessTransactionCommand command = createCommand(message);

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                eq("event-456"),
                eq("correlation-123"),
                eq(record)
        )).thenReturn(command);

        ArgumentCaptor<UUID> runIdCaptor = ArgumentCaptor.forClass(UUID.class);

        // Act
        consumer.onMessage(record);

        // Assert
        verify(mapper).toCommand(
                eq(message),
                runIdCaptor.capture(),
                eq("event-456"),
                eq("correlation-123"),
                eq(record)
        );

        assertThat(runIdCaptor.getValue()).isNotNull();

        verify(ingestionService).ingest(command);
        verifyNoMoreInteractions(mapper, ingestionService);
    }

    @Test
    @DisplayName("Deve processar mensagem quando os headers não estiverem presentes")
    void shouldProcessMessageWhenHeadersAreMissing() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        ProcessTransactionCommand command = createCommand(message);

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                isNull(),
                isNull(),
                eq(record)
        )).thenReturn(command);

        ArgumentCaptor<UUID> runIdCaptor = ArgumentCaptor.forClass(UUID.class);

        // Act
        consumer.onMessage(record);

        // Assert
        verify(mapper).toCommand(
                eq(message),
                runIdCaptor.capture(),
                isNull(),
                isNull(),
                eq(record)
        );

        assertThat(runIdCaptor.getValue()).isNotNull();

        verify(ingestionService).ingest(command);
    }

    @Test
    @DisplayName("Deve processar mensagem com identificadores nulos quando os valores dos headers forem nulos")
    void shouldProcessMessageWithNullIdentifiersWhenHeaderValuesAreNull() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        record.headers()
                .add(new RecordHeader("correlationId", null))
                .add(new RecordHeader("eventId", null));

        ProcessTransactionCommand command = createCommand(message);

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                isNull(),
                isNull(),
                eq(record)
        )).thenReturn(command);

        // Act
        consumer.onMessage(record);

        // Assert
        verify(mapper).toCommand(
                eq(message),
                any(UUID.class),
                isNull(),
                isNull(),
                eq(record)
        );

        verify(ingestionService).ingest(command);
    }

    @Test
    @DisplayName("Deve preservar header vazio ao converter mensagem recebida")
    void shouldPreserveEmptyHeaderValue() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        record.headers()
                .add("correlationId", new byte[0])
                .add("eventId", new byte[0]);

        ProcessTransactionCommand command = createCommand(message);

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                eq(""),
                eq(""),
                eq(record)
        )).thenReturn(command);

        // Act
        consumer.onMessage(record);

        // Assert
        verify(mapper).toCommand(
                eq(message),
                any(UUID.class),
                eq(""),
                eq(""),
                eq(record)
        );

        verify(ingestionService).ingest(command);
    }

    @Test
    @DisplayName("Deve utilizar o último header quando existirem valores duplicados")
    void shouldUseLastHeaderWhenHeadersAreDuplicated() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        record.headers()
                .add(
                        "correlationId",
                        "correlation-antigo".getBytes(StandardCharsets.UTF_8)
                )
                .add(
                        "correlationId",
                        "correlation-novo".getBytes(StandardCharsets.UTF_8)
                )
                .add(
                        "eventId",
                        "event-antigo".getBytes(StandardCharsets.UTF_8)
                )
                .add(
                        "eventId",
                        "event-novo".getBytes(StandardCharsets.UTF_8)
                );

        ProcessTransactionCommand command = createCommand(message);

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                eq("event-novo"),
                eq("correlation-novo"),
                eq(record)
        )).thenReturn(command);

        // Act
        consumer.onMessage(record);

        // Assert
        verify(mapper).toCommand(
                eq(message),
                any(UUID.class),
                eq("event-novo"),
                eq("correlation-novo"),
                eq(record)
        );

        verify(ingestionService).ingest(command);
    }

    @Test
    @DisplayName("Deve ignorar mensagem quando o payload for nulo")
    void shouldIgnoreMessageWhenPayloadIsNull() {
        // Arrange
        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(null);

        record.headers()
                .add(
                        "correlationId",
                        "correlation-123".getBytes(StandardCharsets.UTF_8)
                )
                .add(
                        "eventId",
                        "event-456".getBytes(StandardCharsets.UTF_8)
                );

        // Act
        consumer.onMessage(record);

        // Assert
        verifyNoInteractions(mapper, ingestionService);
    }

    @Test
    @DisplayName("Deve ignorar payload nulo mesmo quando os headers não existirem")
    void shouldIgnoreNullPayloadWhenHeadersAreMissing() {
        // Arrange
        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(null);

        // Act
        consumer.onMessage(record);

        // Assert
        verifyNoInteractions(mapper, ingestionService);
    }

    @Test
    @DisplayName("Deve propagar exceção do mapper e não executar o serviço de ingestão")
    void shouldPropagateMapperExceptionAndNotInvokeIngestionService() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        record.headers()
                .add(
                        "correlationId",
                        "correlation-123".getBytes(StandardCharsets.UTF_8)
                )
                .add(
                        "eventId",
                        "event-456".getBytes(StandardCharsets.UTF_8)
                );

        IllegalArgumentException exception =
                new IllegalArgumentException("Erro ao mapear mensagem");

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                eq("event-456"),
                eq("correlation-123"),
                eq(record)
        )).thenThrow(exception);

        // Act / Assert
        assertThatThrownBy(() -> consumer.onMessage(record))
                .isSameAs(exception)
                .hasMessage("Erro ao mapear mensagem");

        verify(mapper).toCommand(
                eq(message),
                any(UUID.class),
                eq("event-456"),
                eq("correlation-123"),
                eq(record)
        );

        verifyNoInteractions(ingestionService);
    }

    @Test
    @DisplayName("Deve propagar exceção do serviço de ingestão")
    void shouldPropagateIngestionServiceException() {
        // Arrange
        CategorizedTransactionMessage message = createMessage();

        ConsumerRecord<String, CategorizedTransactionMessage> record =
                createRecord(message);

        ProcessTransactionCommand command = createCommand(message);

        when(mapper.toCommand(
                eq(message),
                any(UUID.class),
                isNull(),
                isNull(),
                eq(record)
        )).thenReturn(command);

        RuntimeException exception =
                new RuntimeException("Erro ao processar transação");

        doThrow(exception)
                .when(ingestionService)
                .ingest(command);

        // Act / Assert
        assertThatThrownBy(() -> consumer.onMessage(record))
                .isSameAs(exception)
                .hasMessage("Erro ao processar transação");

        verify(mapper).toCommand(
                eq(message),
                any(UUID.class),
                isNull(),
                isNull(),
                eq(record)
        );

        verify(ingestionService).ingest(command);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o registro recebido for nulo")
    void shouldThrowExceptionWhenRecordIsNull() {
        // Arrange
        ConsumerRecord<String, CategorizedTransactionMessage> record = null;

        // Act / Assert
        assertThatThrownBy(() -> consumer.onMessage(record))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(mapper, ingestionService);
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

    private ConsumerRecord<String, CategorizedTransactionMessage> createRecord(
            CategorizedTransactionMessage message
    ) {
        return new ConsumerRecord<>(
                "categorized-transactions",
                1,
                42L,
                "transaction-key",
                message
        );
    }

    private ProcessTransactionCommand createCommand(
            CategorizedTransactionMessage message
    ) {
        return new ProcessTransactionCommand(
                UUID.randomUUID(),
                message.transactionId(),
                message.userId(),
                message.categoryId(),
                message.amount(),
                message.currency(),
                message.transactionDate(),
                new ProcessTransactionCommand.EventMetadata(
                        "event-456",
                        "correlation-123",
                        "categorized-transactions",
                        1,
                        42L,
                        "transaction-key",
                        Instant.EPOCH
                )
        );
    }
}