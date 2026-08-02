package br.com.ecofy.ms_notification.adapters.in.kafka.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes unitários de InsightCreatedEventMessage")
class InsightCreatedEventMessageTest {

    @Test
    @DisplayName("Deve preservar todos os campos quando a mensagem for criada com dados válidos")
    void devePreservarTodosOsCamposQuandoMensagemForCriadaComDadosValidos() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );
        var insightType = "MONTHLY_SPENDING";
        var periodStart = "2026-08-01";
        var periodEnd = "2026-08-31";

        // Act
        var message = new InsightCreatedEventMessage(
                userId,
                insightId,
                insightType,
                periodStart,
                periodEnd,
                null
        );

        // Assert
        assertAll(
                () -> assertEquals(userId, message.userId()),
                () -> assertEquals(insightId, message.insightId()),
                () -> assertEquals(insightType, message.insightType()),
                () -> assertEquals(periodStart, message.periodStart()),
                () -> assertEquals(periodEnd, message.periodEnd()),
                () -> assertNull(message.metadata())
        );
    }

    @Test
    @DisplayName("Deve aceitar valores nulos quando a mensagem for criada sem validações")
    void deveAceitarValoresNulosQuandoMensagemForCriadaSemValidacoes() {
        // Arrange

        // Act
        var message = new InsightCreatedEventMessage(
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Assert
        assertAll(
                () -> assertNull(message.userId()),
                () -> assertNull(message.insightId()),
                () -> assertNull(message.insightType()),
                () -> assertNull(message.periodStart()),
                () -> assertNull(message.periodEnd()),
                () -> assertNull(message.metadata())
        );
    }

    @Test
    @DisplayName("Deve aceitar textos vazios e em branco quando não existirem validações")
    void deveAceitarTextosVaziosEEmBrancoQuandoNaoExistiremValidacoes() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        // Act
        var message = new InsightCreatedEventMessage(
                userId,
                insightId,
                "",
                " ",
                "   ",
                null
        );

        // Assert
        assertAll(
                () -> assertEquals("", message.insightType()),
                () -> assertEquals(" ", message.periodStart()),
                () -> assertEquals("   ", message.periodEnd())
        );
    }

    @Test
    @DisplayName("Deve considerar mensagens iguais quando todos os campos forem equivalentes")
    void deveConsiderarMensagensIguaisQuandoTodosOsCamposForemEquivalentes() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        var firstMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        var equivalentMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        // Act
        var firstHashCode = firstMessage.hashCode();
        var secondHashCode = equivalentMessage.hashCode();

        // Assert
        assertAll(
                () -> assertEquals(firstMessage, firstMessage),
                () -> assertEquals(firstMessage, equivalentMessage),
                () -> assertEquals(equivalentMessage, firstMessage),
                () -> assertEquals(firstHashCode, secondHashCode)
        );
    }

    @Test
    @DisplayName("Deve considerar mensagens diferentes quando o identificador do usuário for diferente")
    void deveConsiderarMensagensDiferentesQuandoIdentificadorDoUsuarioForDiferente() {
        // Arrange
        var firstMessage = new InsightCreatedEventMessage(
                UUID.fromString("22cb67d2-1637-44f5-aa31-cf8c1af76887"),
                UUID.fromString("a7710acc-d741-4fa6-ab2a-923dfc70f87f"),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        var differentMessage = new InsightCreatedEventMessage(
                UUID.fromString("c59732c2-cafd-40e6-93d5-3c8ad0712df1"),
                UUID.fromString("a7710acc-d741-4fa6-ab2a-923dfc70f87f"),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        // Act
        var result = firstMessage.equals(differentMessage);

        // Assert
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Deve considerar mensagens diferentes quando o identificador do insight for diferente")
    void deveConsiderarMensagensDiferentesQuandoIdentificadorDoInsightForDiferente() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );

        var firstMessage = new InsightCreatedEventMessage(
                userId,
                UUID.fromString("a7710acc-d741-4fa6-ab2a-923dfc70f87f"),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        var differentMessage = new InsightCreatedEventMessage(
                userId,
                UUID.fromString("1ae32a47-617d-4882-b8e7-da63a11e1cd6"),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        // Act
        var result = firstMessage.equals(differentMessage);

        // Assert
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Deve considerar mensagens diferentes quando o tipo do insight for diferente")
    void deveConsiderarMensagensDiferentesQuandoTipoDoInsightForDiferente() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        var firstMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        var differentMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "BUDGET_RISK",
                "2026-08-01",
                "2026-08-31",
                null
        );

        // Act
        var result = firstMessage.equals(differentMessage);

        // Assert
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Deve considerar mensagens diferentes quando o início do período for diferente")
    void deveConsiderarMensagensDiferentesQuandoInicioDoPeriodoForDiferente() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        var firstMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        var differentMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-07-01",
                "2026-08-31",
                null
        );

        // Act
        var result = firstMessage.equals(differentMessage);

        // Assert
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Deve considerar mensagens diferentes quando o fim do período for diferente")
    void deveConsiderarMensagensDiferentesQuandoFimDoPeriodoForDiferente() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        var firstMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        var differentMessage = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-09-30",
                null
        );

        // Act
        var result = firstMessage.equals(differentMessage);

        // Assert
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Deve considerar a mensagem diferente quando for comparada com nulo ou outro tipo")
    void deveConsiderarMensagemDiferenteQuandoComparadaComNuloOuOutroTipo() {
        // Arrange
        var message = new InsightCreatedEventMessage(
                UUID.fromString("22cb67d2-1637-44f5-aa31-cf8c1af76887"),
                UUID.fromString("a7710acc-d741-4fa6-ab2a-923dfc70f87f"),
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        // Act
        var equalsNull = message.equals(null);
        var equalsOtherType = message.equals("MONTHLY_SPENDING");

        // Assert
        assertAll(
                () -> assertEquals(false, equalsNull),
                () -> assertEquals(false, equalsOtherType),
                () -> assertNotEquals(null, message),
                () -> assertNotEquals("MONTHLY_SPENDING", message)
        );
    }

    @Test
    @DisplayName("Deve representar os campos quando a mensagem for convertida para texto")
    void deveRepresentarOsCamposQuandoMensagemForConvertidaParaTexto() {
        // Arrange
        var userId = UUID.fromString(
                "22cb67d2-1637-44f5-aa31-cf8c1af76887"
        );
        var insightId = UUID.fromString(
                "a7710acc-d741-4fa6-ab2a-923dfc70f87f"
        );

        var message = new InsightCreatedEventMessage(
                userId,
                insightId,
                "MONTHLY_SPENDING",
                "2026-08-01",
                "2026-08-31",
                null
        );

        // Act
        var result = message.toString();

        // Assert
        assertAll(
                () -> assertTrue(
                        result.startsWith("InsightCreatedEventMessage[")
                ),
                () -> assertTrue(result.contains("userId=" + userId)),
                () -> assertTrue(result.contains("insightId=" + insightId)),
                () -> assertTrue(
                        result.contains("insightType=MONTHLY_SPENDING")
                ),
                () -> assertTrue(
                        result.contains("periodStart=2026-08-01")
                ),
                () -> assertTrue(
                        result.contains("periodEnd=2026-08-31")
                ),
                () -> assertTrue(result.contains("metadata=null"))
        );
    }
}