package br.com.ecofy.ms_notification.adapters.in.kafka.mapper;

import br.com.ecofy.ms_notification.adapters.in.kafka.dto.BudgetAlertEventMessage;
import br.com.ecofy.ms_notification.adapters.in.kafka.dto.InsightCreatedEventMessage;
import br.com.ecofy.ms_notification.adapters.in.kafka.dto.MessageMetadata;
import br.com.ecofy.ms_notification.core.domain.enums.DomainEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes unitários de InboundEventMapper")
class InboundEventMapperTest {

    private InboundEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InboundEventMapper();
    }

    @Test
    @DisplayName("Deve mapear alerta de orçamento quando todos os dados forem válidos")
    void deveMapearAlertaDeOrcamentoQuandoTodosOsDadosForemValidos() {
        // Arrange
        var eventId = UUID.fromString(
                "f9573420-4d29-4a37-8d6f-289fd1c9d6c7"
        );
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var budgetId = UUID.fromString(
                "44e27041-7801-4345-8284-989159f842f6"
        );
        var categoryId = UUID.fromString(
                "354c6df3-c07e-4a61-8421-4ec203cf6a55"
        );
        var limitAmount = new BigDecimal("1000.00");
        var consumedAmount = new BigDecimal("850.50");
        var percentageConsumed = new BigDecimal("85.05");

        var data = new BudgetAlertEventMessage.Data(
                userId,
                budgetId,
                categoryId,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                limitAmount,
                consumedAmount,
                percentageConsumed,
                "BRL",
                "WARNING"
        );

        var message = new BudgetAlertEventMessage(
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

        // Act
        var command = mapper.fromBudgetAlert(message);

        // Assert
        assertAll(
                () -> assertEquals(
                        DomainEventType.BUDGET_ALERT,
                        command.eventType()
                ),
                () -> assertEquals(userId, command.userId()),
                () -> assertEquals(
                        eventId.toString(),
                        command.idempotencyKey()
                ),
                () -> assertEquals(6, command.payload().size()),
                () -> assertEquals(
                        budgetId,
                        command.payload().get("budgetId")
                ),
                () -> assertEquals(
                        categoryId,
                        command.payload().get("categoryId")
                ),
                () -> assertEquals(
                        limitAmount,
                        command.payload().get("limitAmount")
                ),
                () -> assertEquals(
                        consumedAmount,
                        command.payload().get("consumedAmount")
                ),
                () -> assertEquals(
                        percentageConsumed,
                        command.payload().get("consumedPct")
                ),
                () -> assertEquals(
                        "WARNING",
                        command.payload().get("severity")
                ),
                () -> assertFalse(
                        command.payload().containsKey("periodStart")
                ),
                () -> assertFalse(
                        command.payload().containsKey("periodEnd")
                ),
                () -> assertFalse(
                        command.payload().containsKey("currency")
                )
        );
    }

    @Test
    @DisplayName("Deve ignorar campos nulos quando o alerta de orçamento possuir somente usuário")
    void deveIgnorarCamposNulosQuandoAlertaDeOrcamentoPossuirSomenteUsuario() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );

        var data = new BudgetAlertEventMessage.Data(
                userId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var message = new BudgetAlertEventMessage(
                null,
                "BUDGET_ALERT",
                1,
                null,
                null,
                null,
                null,
                data
        );

        // Act
        var command = mapper.fromBudgetAlert(message);

        // Assert
        assertAll(
                () -> assertEquals(
                        DomainEventType.BUDGET_ALERT,
                        command.eventType()
                ),
                () -> assertEquals(userId, command.userId()),
                () -> assertTrue(command.payload().isEmpty()),
                () -> assertEquals(
                        command.idempotencyKey(),
                        UUID.fromString(
                                command.idempotencyKey()
                        ).toString()
                )
        );
    }

    @Test
    @DisplayName("Deve produzir payload imutável quando o alerta de orçamento for mapeado")
    void deveProduzirPayloadImutavelQuandoAlertaDeOrcamentoForMapeado() {
        // Arrange
        var data = new BudgetAlertEventMessage.Data(
                UUID.fromString(
                        "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                ),
                UUID.fromString(
                        "44e27041-7801-4345-8284-989159f842f6"
                ),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var message = new BudgetAlertEventMessage(
                UUID.fromString(
                        "f9573420-4d29-4a37-8d6f-289fd1c9d6c7"
                ),
                null,
                null,
                null,
                null,
                null,
                null,
                data
        );

        var command = mapper.fromBudgetAlert(message);

        // Act
        var exception = assertThrows(
                UnsupportedOperationException.class,
                () -> command.payload().put("newField", "newValue")
        );

        // Assert
        assertEquals(
                UUID.fromString(
                        "44e27041-7801-4345-8284-989159f842f6"
                ),
                command.payload().get("budgetId")
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando o alerta de orçamento for nulo")
    void deveLancarExcecaoQuandoAlertaDeOrcamentoForNulo() {
        // Arrange

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> mapper.fromBudgetAlert(null)
        );

        // Assert
        assertEquals("msg must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando os dados do alerta de orçamento forem nulos")
    void deveLancarExcecaoQuandoDadosDoAlertaDeOrcamentoForemNulos() {
        // Arrange
        var message = new BudgetAlertEventMessage(
                UUID.randomUUID(),
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                "correlation-123",
                null,
                null
        );

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> mapper.fromBudgetAlert(message)
        );

        // Assert
        assertEquals(
                "budget alert data must not be null",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário do alerta de orçamento for nulo")
    void deveLancarExcecaoQuandoUsuarioDoAlertaDeOrcamentoForNulo() {
        // Arrange
        var data = new BudgetAlertEventMessage.Data(
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                "BRL",
                "WARNING"
        );

        var message = new BudgetAlertEventMessage(
                UUID.randomUUID(),
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                "correlation-123",
                null,
                data
        );

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> mapper.fromBudgetAlert(message)
        );

        // Assert
        assertEquals(
                "userId must not be null",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve mapear insight criado quando todos os dados forem válidos")
    void deveMapearInsightCriadoQuandoTodosOsDadosForemValidos() {
        // Arrange
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

        // Act
        var command = mapper.fromInsightCreated(message);

        // Assert
        assertAll(
                () -> assertEquals(
                        DomainEventType.INSIGHT_CREATED,
                        command.eventType()
                ),
                () -> assertEquals(userId, command.userId()),
                () -> assertEquals(
                        "event-123",
                        command.idempotencyKey()
                ),
                () -> assertEquals(4, command.payload().size()),
                () -> assertEquals(
                        insightId,
                        command.payload().get("insightId")
                ),
                () -> assertEquals(
                        "MONTHLY_SPENDING",
                        command.payload().get("insightType")
                ),
                () -> assertEquals(
                        "2026-08-01",
                        command.payload().get("periodStart")
                ),
                () -> assertEquals(
                        "2026-08-31",
                        command.payload().get("periodEnd")
                )
        );
    }

    @Test
    @DisplayName("Deve ignorar campos nulos quando o insight possuir somente usuário")
    void deveIgnorarCamposNulosQuandoInsightPossuirSomenteUsuario() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );

        var message = new InsightCreatedEventMessage(
                userId,
                null,
                null,
                null,
                null,
                null
        );

        // Act
        var command = mapper.fromInsightCreated(message);

        // Assert
        assertAll(
                () -> assertEquals(
                        DomainEventType.INSIGHT_CREATED,
                        command.eventType()
                ),
                () -> assertEquals(userId, command.userId()),
                () -> assertTrue(command.payload().isEmpty()),
                () -> assertEquals(
                        command.idempotencyKey(),
                        UUID.fromString(
                                command.idempotencyKey()
                        ).toString()
                )
        );
    }

    @ParameterizedTest(
            name = "[{index}] Deve gerar idempotência para eventId em branco: \"{0}\""
    )
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("Deve gerar identificador aleatório quando o eventId do insight estiver em branco")
    void deveGerarIdentificadorAleatorioQuandoEventIdDoInsightEstiverEmBranco(
            String eventId
    ) {
        // Arrange
        var metadata = new MessageMetadata(
                eventId,
                "correlation-456",
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-insights"
        );

        var message = new InsightCreatedEventMessage(
                UUID.fromString(
                        "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                ),
                null,
                null,
                null,
                null,
                metadata
        );

        // Act
        var command = mapper.fromInsightCreated(message);

        // Assert
        assertAll(
                () -> assertFalse(command.idempotencyKey().isBlank()),
                () -> assertEquals(
                        command.idempotencyKey(),
                        UUID.fromString(
                                command.idempotencyKey()
                        ).toString()
                )
        );
    }

    @Test
    @DisplayName("Deve gerar identificador aleatório quando os metadados possuírem eventId nulo")
    void deveGerarIdentificadorAleatorioQuandoMetadadosPossuiremEventIdNulo() {
        // Arrange
        var metadata = new MessageMetadata(
                null,
                "correlation-456",
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-insights"
        );

        var message = new InsightCreatedEventMessage(
                UUID.fromString(
                        "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                ),
                null,
                null,
                null,
                null,
                metadata
        );

        // Act
        var command = mapper.fromInsightCreated(message);

        // Assert
        assertEquals(
                command.idempotencyKey(),
                UUID.fromString(command.idempotencyKey()).toString()
        );
    }

    @Test
    @DisplayName("Deve produzir payload imutável quando o insight for mapeado")
    void deveProduzirPayloadImutavelQuandoInsightForMapeado() {
        // Arrange
        var message = new InsightCreatedEventMessage(
                UUID.fromString(
                        "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                ),
                UUID.fromString(
                        "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
                ),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                new MessageMetadata(
                        "event-123",
                        null,
                        Instant.parse("2026-08-02T12:00:00Z"),
                        "ms-insights"
                )
        );

        var command = mapper.fromInsightCreated(message);

        // Act
        var exception = assertThrows(
                UnsupportedOperationException.class,
                () -> command.payload().remove("insightId")
        );

        // Assert
        assertEquals(
                UUID.fromString(
                        "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
                ),
                command.payload().get("insightId")
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando o evento de insight for nulo")
    void deveLancarExcecaoQuandoEventoDeInsightForNulo() {
        // Arrange

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> mapper.fromInsightCreated(null)
        );

        // Assert
        assertEquals("msg must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário do insight for nulo")
    void deveLancarExcecaoQuandoUsuarioDoInsightForNulo() {
        // Arrange
        var message = new InsightCreatedEventMessage(
                null,
                UUID.randomUUID(),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                MessageMetadata.minimal()
        );

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> mapper.fromInsightCreated(message)
        );

        // Assert
        assertEquals(
                "userId must not be null",
                exception.getMessage()
        );
    }
}