package br.com.ecofy.ms_notification.adapters.in.kafka;

import br.com.ecofy.ms_notification.adapters.in.kafka.dto.InsightCreatedEventMessage;
import br.com.ecofy.ms_notification.adapters.in.kafka.dto.MessageMetadata;
import br.com.ecofy.ms_notification.adapters.in.kafka.mapper.InboundEventMapper;
import br.com.ecofy.ms_notification.core.application.command.HandleDomainEventCommand;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import br.com.ecofy.ms_notification.core.port.in.HandleDomainEventNotificationUseCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de InsightCreatedEventConsumer")
class InsightCreatedEventConsumerTest {

    @Mock
    private InboundEventMapper mapper;

    @Mock
    private HandleDomainEventNotificationUseCase useCase;

    @InjectMocks
    private InsightCreatedEventConsumer consumer;

    private Logger consumerLogger;
    private Level originalLogLevel;

    @BeforeEach
    void setUp() {
        consumerLogger = (Logger) LoggerFactory.getLogger(
                InsightCreatedEventConsumer.class
        );
        originalLogLevel = consumerLogger.getLevel();
    }

    @AfterEach
    void tearDown() {
        consumerLogger.setLevel(originalLogLevel);
    }

    @Test
    @DisplayName("Deve mapear e processar o insight quando a mensagem e os headers forem válidos")
    void deveMapearEProcessarInsightQuandoMensagemEHeadersForemValidos() {
        // Arrange
        consumerLogger.setLevel(Level.DEBUG);

        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        var metadata = new MessageMetadata(
                "event-123",
                "correlation-456",
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-insights"
        );

        var message = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                metadata
        );

        var command = new HandleDomainEventCommand(
                DomainEventType.INSIGHT_CREATED,
                userId,
                Map.of(
                        "insightId", insightId,
                        "insightType", "MONTHLY_SPENDING",
                        "periodStart", "2026-08-01",
                        "periodEnd", "2026-08-31"
                ),
                "event-123"
        );

        when(mapper.fromInsightCreated(message)).thenReturn(command);

        // Act
        assertDoesNotThrow(
                () -> consumer.consume(
                        message,
                        "eco.insight.created",
                        2,
                        150L
                )
        );

        // Assert
        verify(mapper).fromInsightCreated(message);
        verify(useCase).handle(command);
        verifyNoMoreInteractions(mapper, useCase);
    }

    @Test
    @DisplayName("Deve processar comando nulo quando metadados e headers não forem informados")
    void deveProcessarComandoNuloQuandoMetadadosEHeadersNaoForemInformados() {
        // Arrange
        consumerLogger.setLevel(Level.INFO);

        var message = new InsightCreatedEventMessage(
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(mapper.fromInsightCreated(message)).thenReturn(null);

        // Act
        assertDoesNotThrow(
                () -> consumer.consume(
                        message,
                        null,
                        null,
                        null
                )
        );

        // Assert
        verify(mapper).fromInsightCreated(message);
        verify(useCase).handle(null);
        verifyNoMoreInteractions(mapper, useCase);
    }

    @Test
    @DisplayName("Deve processar insight quando os metadados possuírem identificador nulo")
    void deveProcessarInsightQuandoMetadadosPossuiremIdentificadorNulo() {
        // Arrange
        consumerLogger.setLevel(Level.INFO);

        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );

        var metadata = new MessageMetadata(
                null,
                "correlation-456",
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-insights"
        );

        var message = new InsightCreatedEventMessage(
                userId,
                null,
                null,
                null,
                null,
                metadata
        );

        var command = new HandleDomainEventCommand(
                DomainEventType.INSIGHT_CREATED,
                userId,
                Map.of(),
                "generated-idempotency-key"
        );

        when(mapper.fromInsightCreated(message)).thenReturn(command);

        // Act
        assertDoesNotThrow(
                () -> consumer.consume(
                        message,
                        "eco.insight.created",
                        0,
                        0L
                )
        );

        // Assert
        verify(mapper).fromInsightCreated(message);
        verify(useCase).handle(command);
        verifyNoMoreInteractions(mapper, useCase);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a mensagem recebida for nula")
    void deveLancarExcecaoQuandoMensagemRecebidaForNula() {
        // Arrange

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> consumer.consume(
                        null,
                        "eco.insight.created",
                        1,
                        10L
                )
        );

        // Assert
        assertEquals(
                "message must not be null",
                exception.getMessage()
        );
        verifyNoInteractions(mapper, useCase);
    }

    @Test
    @DisplayName("Deve propagar exceção quando o mapper falhar")
    void devePropagarExcecaoQuandoMapperFalhar() {
        // Arrange
        consumerLogger.setLevel(Level.INFO);

        var message = createMessage();

        var expectedException = new IllegalArgumentException(
                "Evento de insight inválido"
        );

        when(mapper.fromInsightCreated(message))
                .thenThrow(expectedException);

        // Act
        var actualException = assertThrows(
                IllegalArgumentException.class,
                () -> consumer.consume(
                        message,
                        "eco.insight.created",
                        2,
                        150L
                )
        );

        // Assert
        assertSame(expectedException, actualException);
        verify(mapper).fromInsightCreated(message);
        verifyNoInteractions(useCase);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("Deve propagar exceção quando o caso de uso falhar")
    void devePropagarExcecaoQuandoCasoDeUsoFalhar() {
        // Arrange
        consumerLogger.setLevel(Level.INFO);

        var message = createMessage();
        var userId = message.userId();

        var command = new HandleDomainEventCommand(
                DomainEventType.INSIGHT_CREATED,
                userId,
                Map.of(
                        "insightId", message.insightId(),
                        "insightType", message.insightType()
                ),
                message.metadata().eventId()
        );

        var expectedException = new IllegalStateException(
                "Falha ao processar notificação"
        );

        when(mapper.fromInsightCreated(message)).thenReturn(command);
        doThrow(expectedException)
                .when(useCase)
                .handle(command);

        // Act
        var actualException = assertThrows(
                IllegalStateException.class,
                () -> consumer.consume(
                        message,
                        "eco.insight.created",
                        2,
                        150L
                )
        );

        // Assert
        assertSame(expectedException, actualException);
        verify(mapper).fromInsightCreated(message);
        verify(useCase).handle(command);
        verifyNoMoreInteractions(mapper, useCase);
    }

    private static InsightCreatedEventMessage createMessage() {
        var metadata = new MessageMetadata(
                "event-123",
                "correlation-456",
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-insights"
        );

        return new InsightCreatedEventMessage(
                UUID.fromString(
                        "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                ),
                UUID.fromString(
                        "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
                ),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                metadata
        );
    }
}