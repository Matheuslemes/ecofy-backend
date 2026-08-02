package br.com.ecofy.ms_notification.adapters.in.kafka.dto;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes unitários de BudgetAlertEventMessage")
class BudgetAlertEventMessageTest {

    @Test
    @DisplayName("Deve preservar todos os campos quando a mensagem for criada com dados válidos")
    void devePreservarTodosOsCamposQuandoMensagemForCriadaComDadosValidos() {
        // Arrange
        var eventId = UUID.fromString("f9573420-4d29-4a37-8d6f-289fd1c9d6c7");
        var causationId = UUID.fromString("36b44330-9cb9-463c-af1b-b24239b4a20a");
        var userId = UUID.fromString("22cb67d2-1637-44f5-aa31-cf8c1af76887");
        var budgetId = UUID.fromString("44e27041-7801-4345-8284-989159f842f6");
        var categoryId = UUID.fromString("354c6df3-c07e-4a61-8421-4ec203cf6a55");
        var occurredAt = Instant.parse("2026-08-02T12:00:00Z");
        var periodStart = LocalDate.of(2026, 8, 1);
        var periodEnd = LocalDate.of(2026, 8, 31);
        var limitAmount = new BigDecimal("1000.00");
        var consumedAmount = new BigDecimal("850.50");
        var percentageConsumed = new BigDecimal("85.05");

        var data = new BudgetAlertEventMessage.Data(
                userId,
                budgetId,
                categoryId,
                periodStart,
                periodEnd,
                limitAmount,
                consumedAmount,
                percentageConsumed,
                "BRL",
                "WARNING"
        );

        // Act
        var message = new BudgetAlertEventMessage(
                eventId,
                "BUDGET_ALERT",
                1,
                occurredAt,
                "ms-budgeting",
                "correlation-123",
                causationId,
                data
        );

        // Assert
        assertAll(
                () -> assertEquals(eventId, message.eventId()),
                () -> assertEquals("BUDGET_ALERT", message.eventType()),
                () -> assertEquals(1, message.eventVersion()),
                () -> assertEquals(occurredAt, message.occurredAt()),
                () -> assertEquals("ms-budgeting", message.producer()),
                () -> assertEquals("correlation-123", message.correlationId()),
                () -> assertEquals(causationId, message.causationId()),
                () -> assertEquals(data, message.data()),
                () -> assertEquals(userId, message.data().userId()),
                () -> assertEquals(budgetId, message.data().budgetId()),
                () -> assertEquals(categoryId, message.data().categoryId()),
                () -> assertEquals(periodStart, message.data().periodStart()),
                () -> assertEquals(periodEnd, message.data().periodEnd()),
                () -> assertEquals(limitAmount, message.data().limitAmount()),
                () -> assertEquals(consumedAmount, message.data().consumedAmount()),
                () -> assertEquals(
                        percentageConsumed,
                        message.data().percentageConsumed()
                ),
                () -> assertEquals("BRL", message.data().currency()),
                () -> assertEquals("WARNING", message.data().alertLevel())
        );
    }

    @Test
    @DisplayName("Deve aceitar valores nulos quando a mensagem for criada sem validações")
    void deveAceitarValoresNulosQuandoMensagemForCriadaSemValidacoes() {
        // Arrange
        var data = new BudgetAlertEventMessage.Data(
                null,
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

        // Act
        var message = new BudgetAlertEventMessage(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                data
        );

        // Assert
        assertAll(
                () -> assertNull(message.eventId()),
                () -> assertNull(message.eventType()),
                () -> assertNull(message.eventVersion()),
                () -> assertNull(message.occurredAt()),
                () -> assertNull(message.producer()),
                () -> assertNull(message.correlationId()),
                () -> assertNull(message.causationId()),
                () -> assertNotNull(message.data()),
                () -> assertNull(message.data().userId()),
                () -> assertNull(message.data().budgetId()),
                () -> assertNull(message.data().categoryId()),
                () -> assertNull(message.data().periodStart()),
                () -> assertNull(message.data().periodEnd()),
                () -> assertNull(message.data().limitAmount()),
                () -> assertNull(message.data().consumedAmount()),
                () -> assertNull(message.data().percentageConsumed()),
                () -> assertNull(message.data().currency()),
                () -> assertNull(message.data().alertLevel())
        );
    }

    @Test
    @DisplayName("Deve manter o contrato de igualdade quando mensagens equivalentes forem comparadas")
    void deveManterContratoDeIgualdadeQuandoMensagensEquivalentesForemComparadas() {
        // Arrange
        var eventId = UUID.fromString("f9573420-4d29-4a37-8d6f-289fd1c9d6c7");
        var userId = UUID.fromString("22cb67d2-1637-44f5-aa31-cf8c1af76887");

        var firstData = new BudgetAlertEventMessage.Data(
                userId,
                UUID.fromString("44e27041-7801-4345-8284-989159f842f6"),
                UUID.fromString("354c6df3-c07e-4a61-8421-4ec203cf6a55"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("1000.00"),
                new BigDecimal("850.50"),
                new BigDecimal("85.05"),
                "BRL",
                "WARNING"
        );

        var secondData = new BudgetAlertEventMessage.Data(
                userId,
                UUID.fromString("44e27041-7801-4345-8284-989159f842f6"),
                UUID.fromString("354c6df3-c07e-4a61-8421-4ec203cf6a55"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("1000.00"),
                new BigDecimal("850.50"),
                new BigDecimal("85.05"),
                "BRL",
                "WARNING"
        );

        var firstMessage = new BudgetAlertEventMessage(
                eventId,
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                "correlation-123",
                null,
                firstData
        );

        var equivalentMessage = new BudgetAlertEventMessage(
                eventId,
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                "correlation-123",
                null,
                secondData
        );

        var differentMessage = new BudgetAlertEventMessage(
                UUID.fromString("2f048b20-0871-49c4-a3ef-9a2c92f9217f"),
                "BUDGET_ALERT",
                1,
                Instant.parse("2026-08-02T12:00:00Z"),
                "ms-budgeting",
                "correlation-123",
                null,
                secondData
        );

        // Act
        var messageText = firstMessage.toString();
        var dataText = firstData.toString();

        // Assert
        assertAll(
                () -> assertEquals(firstMessage, firstMessage),
                () -> assertEquals(firstMessage, equivalentMessage),
                () -> assertEquals(
                        firstMessage.hashCode(),
                        equivalentMessage.hashCode()
                ),
                () -> assertNotEquals(firstMessage, differentMessage),
                () -> assertNotEquals(firstMessage, null),
                () -> assertNotEquals(firstMessage, "BUDGET_ALERT"),
                () -> assertEquals(firstData, secondData),
                () -> assertEquals(firstData.hashCode(), secondData.hashCode()),
                () -> assertNotEquals(firstData, null),
                () -> assertNotEquals(firstData, "Data"),
                () -> assertTrue(messageText.contains("eventType=BUDGET_ALERT")),
                () -> assertTrue(messageText.contains("producer=ms-budgeting")),
                () -> assertTrue(dataText.contains("currency=BRL")),
                () -> assertTrue(dataText.contains("alertLevel=WARNING"))
        );
    }

    @Test
    @DisplayName("Deve ignorar campos desconhecidos quando o evento for desserializado")
    void deveIgnorarCamposDesconhecidosQuandoEventoForDesserializado()
            throws Exception {
        // Arrange
        var objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        var json = """
                {
                  "eventId": "f9573420-4d29-4a37-8d6f-289fd1c9d6c7",
                  "eventType": "BUDGET_ALERT",
                  "eventVersion": 1,
                  "occurredAt": "2026-08-02T12:00:00Z",
                  "producer": "ms-budgeting",
                  "correlationId": "correlation-123",
                  "causationId": "36b44330-9cb9-463c-af1b-b24239b4a20a",
                  "newEnvelopeField": "ignored",
                  "data": {
                    "userId": "22cb67d2-1637-44f5-aa31-cf8c1af76887",
                    "budgetId": "44e27041-7801-4345-8284-989159f842f6",
                    "categoryId": "354c6df3-c07e-4a61-8421-4ec203cf6a55",
                    "periodStart": "2026-08-01",
                    "periodEnd": "2026-08-31",
                    "limitAmount": 1000.00,
                    "consumedAmount": 850.50,
                    "percentageConsumed": 85.05,
                    "currency": "BRL",
                    "alertLevel": "WARNING",
                    "newDataField": "ignored"
                  }
                }
                """;

        // Act
        var message = objectMapper.readValue(
                json,
                BudgetAlertEventMessage.class
        );

        // Assert
        assertAll(
                () -> assertEquals(
                        UUID.fromString(
                                "f9573420-4d29-4a37-8d6f-289fd1c9d6c7"
                        ),
                        message.eventId()
                ),
                () -> assertEquals("BUDGET_ALERT", message.eventType()),
                () -> assertEquals(1, message.eventVersion()),
                () -> assertEquals(
                        Instant.parse("2026-08-02T12:00:00Z"),
                        message.occurredAt()
                ),
                () -> assertEquals("ms-budgeting", message.producer()),
                () -> assertEquals(
                        "correlation-123",
                        message.correlationId()
                ),
                () -> assertEquals(
                        UUID.fromString(
                                "36b44330-9cb9-463c-af1b-b24239b4a20a"
                        ),
                        message.causationId()
                ),
                () -> assertEquals(
                        UUID.fromString(
                                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
                        ),
                        message.data().userId()
                ),
                () -> assertEquals(
                        UUID.fromString(
                                "44e27041-7801-4345-8284-989159f842f6"
                        ),
                        message.data().budgetId()
                ),
                () -> assertEquals(
                        UUID.fromString(
                                "354c6df3-c07e-4a61-8421-4ec203cf6a55"
                        ),
                        message.data().categoryId()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 8, 1),
                        message.data().periodStart()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 8, 31),
                        message.data().periodEnd()
                ),
                () -> assertEquals(
                        new BigDecimal("1000.00"),
                        message.data().limitAmount()
                ),
                () -> assertEquals(
                        new BigDecimal("850.50"),
                        message.data().consumedAmount()
                ),
                () -> assertEquals(
                        new BigDecimal("85.05"),
                        message.data().percentageConsumed()
                ),
                () -> assertEquals("BRL", message.data().currency()),
                () -> assertEquals("WARNING", message.data().alertLevel())
        );
    }
}