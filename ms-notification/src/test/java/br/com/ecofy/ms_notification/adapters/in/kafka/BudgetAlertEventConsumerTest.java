package br.com.ecofy.ms_notification.adapters.in.kafka;

import br.com.ecofy.ms_notification.adapters.in.kafka.dto.BudgetAlertEventMessage;
import br.com.ecofy.ms_notification.adapters.in.kafka.mapper.InboundEventMapper;
import br.com.ecofy.ms_notification.core.application.command.HandleDomainEventCommand;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import br.com.ecofy.ms_notification.core.port.in.HandleDomainEventNotificationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
@DisplayName("Testes unitários de BudgetAlertEventConsumer")
class BudgetAlertEventConsumerTest {

    @Mock
    private InboundEventMapper mapper;

    @Mock
    private HandleDomainEventNotificationUseCase useCase;

    @InjectMocks
    private BudgetAlertEventConsumer consumer;

    @Test
    @DisplayName("Deve mapear e processar o evento quando a mensagem e os headers forem válidos")
    void deveMapearEProcessarEventoQuandoMensagemEHeadersForemValidos() {
        // Arrange
        var eventId = UUID.fromString(
                "f9573420-4d29-4a37-8d6f-289fd1c9d6c7"
        );
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var message = createMessage(eventId, userId);

        var command = new HandleDomainEventCommand(
                DomainEventType.BUDGET_ALERT,
                userId,
                Map.of(
                        "budgetId",
                        message.data().budgetId(),
                        "severity",
                        message.data().alertLevel()
                ),
                eventId.toString()
        );

        when(mapper.fromBudgetAlert(message)).thenReturn(command);

        // Act
        assertDoesNotThrow(
                () -> consumer.consume(
                        message,
                        "eco.budget.alert",
                        2,
                        150L
                )
        );

        // Assert
        verify(mapper).fromBudgetAlert(message);
        verify(useCase).handle(command);
        verifyNoMoreInteractions(mapper, useCase);
    }

    @Test
    @DisplayName("Deve processar comando nulo quando a mensagem não possuir dados nem identificador")
    void deveProcessarComandoNuloQuandoMensagemNaoPossuirDadosNemIdentificador() {
        // Arrange
        var message = new BudgetAlertEventMessage(
                null,
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                null,
                null,
                null
        );

        when(mapper.fromBudgetAlert(message)).thenReturn(null);

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
        verify(mapper).fromBudgetAlert(message);
        verify(useCase).handle(null);
        verifyNoMoreInteractions(mapper, useCase);
    }

    @Test
    @DisplayName("Deve processar evento quando o identificador e o usuário forem nulos")
    void deveProcessarEventoQuandoIdentificadorEUsuarioForemNulos() {
        // Arrange
        var message = createMessage(null, null);

        var command = new HandleDomainEventCommand(
                DomainEventType.BUDGET_ALERT,
                null,
                Map.of(),
                "generated-idempotency-key"
        );

        when(mapper.fromBudgetAlert(message)).thenReturn(command);

        // Act
        assertDoesNotThrow(
                () -> consumer.consume(
                        message,
                        "eco.budget.alert",
                        0,
                        0L
                )
        );

        // Assert
        verify(mapper).fromBudgetAlert(message);
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
                        "eco.budget.alert",
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
        var message = createMessage(
                UUID.fromString(
                        "f9573420-4d29-4a37-8d6f-289fd1c9d6c7"
                ),
                UUID.fromString(
                        "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                )
        );

        var expectedException = new IllegalArgumentException(
                "Evento de orçamento inválido"
        );

        when(mapper.fromBudgetAlert(message))
                .thenThrow(expectedException);

        // Act
        var actualException = assertThrows(
                IllegalArgumentException.class,
                () -> consumer.consume(
                        message,
                        "eco.budget.alert",
                        2,
                        150L
                )
        );

        // Assert
        assertSame(expectedException, actualException);
        verify(mapper).fromBudgetAlert(message);
        verifyNoInteractions(useCase);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("Deve propagar exceção quando o caso de uso falhar")
    void devePropagarExcecaoQuandoCasoDeUsoFalhar() {
        // Arrange
        var eventId = UUID.fromString(
                "f9573420-4d29-4a37-8d6f-289fd1c9d6c7"
        );
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var message = createMessage(eventId, userId);

        var command = new HandleDomainEventCommand(
                DomainEventType.BUDGET_ALERT,
                userId,
                Map.of("severity", "WARNING"),
                eventId.toString()
        );

        var expectedException = new IllegalStateException(
                "Falha ao processar notificação"
        );

        when(mapper.fromBudgetAlert(message)).thenReturn(command);
        doThrow(expectedException)
                .when(useCase)
                .handle(command);

        // Act
        var actualException = assertThrows(
                IllegalStateException.class,
                () -> consumer.consume(
                        message,
                        "eco.budget.alert",
                        2,
                        150L
                )
        );

        // Assert
        assertSame(expectedException, actualException);
        verify(mapper).fromBudgetAlert(message);
        verify(useCase).handle(command);
        verifyNoMoreInteractions(mapper, useCase);
    }

    private static BudgetAlertEventMessage createMessage(
            UUID eventId,
            UUID userId
    ) {
        var data = new BudgetAlertEventMessage.Data(
                userId,
                UUID.fromString(
                        "44e27041-7801-4345-8284-989159f842f6"
                ),
                UUID.fromString(
                        "354c6df3-c07e-4a61-8421-4ec203cf6a55"
                ),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("1000.00"),
                new BigDecimal("850.50"),
                new BigDecimal("85.05"),
                "BRL",
                "WARNING"
        );

        return new BudgetAlertEventMessage(
                eventId,
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                "correlation-123",
                UUID.fromString(
                        "36b44330-9cb9-463c-af1b-b24239b4a20a"
                ),
                data
        );
    }
}