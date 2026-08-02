package br.com.ecofy.ms_notification.adapters.in.kafka.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes unitários de MessageMetadata")
class MessageMetadataTest {

    @Test
    @DisplayName("Deve preservar todos os campos quando os metadados forem criados com dados válidos")
    void devePreservarTodosOsCamposQuandoMetadadosForemCriadosComDadosValidos() {
        // Arrange
        var eventId = "event-123";
        var correlationId = "correlation-456";
        var occurredAt = Instant.parse("2026-08-02T12:00:00Z");
        var source = "ms-insights";

        // Act
        var metadata = new MessageMetadata(
                eventId,
                correlationId,
                occurredAt,
                source
        );

        // Assert
        assertAll(
                () -> assertEquals(eventId, metadata.eventId()),
                () -> assertEquals(
                        correlationId,
                        metadata.correlationId()
                ),
                () -> assertEquals(occurredAt, metadata.occurredAt()),
                () -> assertEquals(source, metadata.source())
        );
    }

    @Test
    @DisplayName("Deve aceitar valores nulos quando os metadados forem criados sem validações")
    void deveAceitarValoresNulosQuandoMetadadosForemCriadosSemValidacoes() {
        // Arrange

        // Act
        var metadata = new MessageMetadata(
                null,
                null,
                null,
                null
        );

        // Assert
        assertAll(
                () -> assertNull(metadata.eventId()),
                () -> assertNull(metadata.correlationId()),
                () -> assertNull(metadata.occurredAt()),
                () -> assertNull(metadata.source())
        );
    }

    @Test
    @DisplayName("Deve aceitar textos vazios e em branco quando não existirem validações")
    void deveAceitarTextosVaziosEEmBrancoQuandoNaoExistiremValidacoes() {
        // Arrange
        var occurredAt = Instant.parse("2026-08-02T12:00:00Z");

        // Act
        var metadata = new MessageMetadata(
                "",
                " ",
                occurredAt,
                "   "
        );

        // Assert
        assertAll(
                () -> assertEquals("", metadata.eventId()),
                () -> assertEquals(" ", metadata.correlationId()),
                () -> assertEquals(occurredAt, metadata.occurredAt()),
                () -> assertEquals("   ", metadata.source())
        );
    }

    @Test
    @DisplayName("Deve criar metadados mínimos quando o método de fábrica for utilizado")
    void deveCriarMetadadosMinimosQuandoMetodoDeFabricaForUtilizado() {
        // Arrange

        // Act
        var metadata = MessageMetadata.minimal();

        // Assert
        assertAll(
                () -> assertNotNull(metadata),
                () -> assertNull(metadata.eventId()),
                () -> assertNull(metadata.correlationId()),
                () -> assertNotNull(metadata.occurredAt()),
                () -> assertEquals("kafka", metadata.source())
        );
    }

    @Test
    @DisplayName("Deve considerar metadados iguais quando todos os campos forem equivalentes")
    void deveConsiderarMetadadosIguaisQuandoTodosOsCamposForemEquivalentes() {
        // Arrange
        var occurredAt = Instant.parse("2026-08-02T12:00:00Z");

        var firstMetadata = new MessageMetadata(
                "event-123",
                "correlation-456",
                occurredAt,
                "ms-insights"
        );

        var equivalentMetadata = new MessageMetadata(
                "event-123",
                "correlation-456",
                occurredAt,
                "ms-insights"
        );

        // Act
        var firstHashCode = firstMetadata.hashCode();
        var secondHashCode = equivalentMetadata.hashCode();

        // Assert
        assertAll(
                () -> assertEquals(firstMetadata, firstMetadata),
                () -> assertEquals(firstMetadata, equivalentMetadata),
                () -> assertEquals(equivalentMetadata, firstMetadata),
                () -> assertEquals(firstHashCode, secondHashCode)
        );
    }

    @Test
    @DisplayName("Deve considerar metadados diferentes quando algum campo for diferente")
    void deveConsiderarMetadadosDiferentesQuandoAlgumCampoForDiferente() {
        // Arrange
        var occurredAt = Instant.parse("2026-08-02T12:00:00Z");

        var metadata = new MessageMetadata(
                "event-123",
                "correlation-456",
                occurredAt,
                "ms-insights"
        );

        var differentEventId = new MessageMetadata(
                "event-999",
                "correlation-456",
                occurredAt,
                "ms-insights"
        );

        var differentCorrelationId = new MessageMetadata(
                "event-123",
                "correlation-999",
                occurredAt,
                "ms-insights"
        );

        var differentOccurredAt = new MessageMetadata(
                "event-123",
                "correlation-456",
                Instant.parse("2026-08-03T12:00:00Z"),
                "ms-insights"
        );

        var differentSource = new MessageMetadata(
                "event-123",
                "correlation-456",
                occurredAt,
                "ms-notification"
        );

        // Act

        // Assert
        assertAll(
                () -> assertNotEquals(metadata, differentEventId),
                () -> assertNotEquals(metadata, differentCorrelationId),
                () -> assertNotEquals(metadata, differentOccurredAt),
                () -> assertNotEquals(metadata, differentSource),
                () -> assertNotEquals(metadata, null),
                () -> assertNotEquals(metadata, "metadata")
        );
    }

    @Test
    @DisplayName("Deve representar todos os campos quando os metadados forem convertidos para texto")
    void deveRepresentarTodosOsCamposQuandoMetadadosForemConvertidosParaTexto() {
        // Arrange
        var occurredAt = Instant.parse("2026-08-02T12:00:00Z");

        var metadata = new MessageMetadata(
                "event-123",
                "correlation-456",
                occurredAt,
                "ms-insights"
        );

        // Act
        var result = metadata.toString();

        // Assert
        assertAll(
                () -> assertTrue(
                        result.startsWith("MessageMetadata[")
                ),
                () -> assertTrue(
                        result.contains("eventId=event-123")
                ),
                () -> assertTrue(
                        result.contains(
                                "correlationId=correlation-456"
                        )
                ),
                () -> assertTrue(
                        result.contains("occurredAt=" + occurredAt)
                ),
                () -> assertTrue(
                        result.contains("source=ms-insights")
                )
        );
    }
}