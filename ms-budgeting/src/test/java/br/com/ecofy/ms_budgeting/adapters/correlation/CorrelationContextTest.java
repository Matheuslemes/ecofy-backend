package br.com.ecofy.ms_budgeting.adapters.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationContextTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Deve retornar falso quando o identificador for nulo")
    void shouldReturnFalseWhenCandidateIsNull() {
        // Arrange
        String candidate = null;

        // Act
        boolean result = CorrelationContext.isValid(candidate);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso quando o identificador estiver vazio")
    void shouldReturnFalseWhenCandidateIsEmpty() {
        // Arrange
        String candidate = "   ";

        // Act
        boolean result = CorrelationContext.isValid(candidate);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso quando o identificador exceder o tamanho máximo")
    void shouldReturnFalseWhenCandidateExceedsMaximumLength() {
        // Arrange
        String candidate = "a".repeat(CorrelationContext.MAX_LENGTH + 1);

        // Act
        boolean result = CorrelationContext.isValid(candidate);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso quando o identificador possuir caracteres inválidos")
    void shouldReturnFalseWhenCandidateContainsUnsafeCharacters() {
        // Arrange
        String candidate = "correlation@id";

        // Act
        boolean result = CorrelationContext.isValid(candidate);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando o identificador possuir tamanho máximo permitido")
    void shouldReturnTrueWhenCandidateHasMaximumAllowedLength() {
        // Arrange
        String candidate = "a".repeat(CorrelationContext.MAX_LENGTH);

        // Act
        boolean result = CorrelationContext.isValid(candidate);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro removendo espaços das extremidades do identificador")
    void shouldReturnTrueWhenTrimmedCandidateIsValid() {
        // Arrange
        String candidate = "  correlation.id_123-test  ";

        // Act
        boolean result = CorrelationContext.isValid(candidate);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve gerar um identificador no formato UUID")
    void shouldGenerateUuid() {
        // Arrange

        // Act
        String result = CorrelationContext.generate();

        // Assert
        assertThat(result).isNotBlank();
        assertThat(UUID.fromString(result).toString()).isEqualTo(result);
    }

    @Test
    @DisplayName("Deve sanitizar e retornar o identificador recebido quando ele for válido")
    void shouldSanitizeReceivedCorrelationIdWhenValid() {
        // Arrange
        String received = "  correlation-id-123  ";

        // Act
        String result = CorrelationContext.sanitizeOrGenerate(received);

        // Assert
        assertThat(result).isEqualTo("correlation-id-123");
    }

    @Test
    @DisplayName("Deve gerar novo identificador quando o valor recebido for inválido")
    void shouldGenerateCorrelationIdWhenReceivedValueIsInvalid() {
        // Arrange
        String received = "correlation id inválido";

        // Act
        String result = CorrelationContext.sanitizeOrGenerate(received);

        // Assert
        assertThat(result).isNotBlank();
        assertThat(UUID.fromString(result).toString()).isEqualTo(result);
        assertThat(result).isNotEqualTo(received);
    }

    @Test
    @DisplayName("Deve gerar novo identificador quando o valor recebido for nulo")
    void shouldGenerateCorrelationIdWhenReceivedValueIsNull() {
        // Arrange
        String received = null;

        // Act
        String result = CorrelationContext.sanitizeOrGenerate(received);

        // Assert
        assertThat(result).isNotBlank();
        assertThat(UUID.fromString(result).toString()).isEqualTo(result);
    }

    @Test
    @DisplayName("Deve retornar o identificador de correlação armazenado no MDC")
    void shouldReturnCurrentCorrelationIdFromMdc() {
        // Arrange
        String correlationId = "correlation-123";
        MDC.put(CorrelationContext.MDC_CORRELATION_KEY, correlationId);

        // Act
        String result = CorrelationContext.currentCorrelationId();

        // Assert
        assertThat(result).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve retornar nulo quando não existir identificador de correlação no MDC")
    void shouldReturnNullWhenCorrelationIdDoesNotExist() {
        // Arrange
        MDC.remove(CorrelationContext.MDC_CORRELATION_KEY);

        // Act
        String result = CorrelationContext.currentCorrelationId();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve retornar o identificador atual quando existir correlação no MDC")
    void shouldReturnCurrentCorrelationIdWhenPresent() {
        // Arrange
        String correlationId = "existing-correlation-id";
        MDC.put(CorrelationContext.MDC_CORRELATION_KEY, correlationId);

        // Act
        String result = CorrelationContext.currentCorrelationIdOrGenerate();

        // Assert
        assertThat(result).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve gerar identificador quando não existir correlação no MDC")
    void shouldGenerateCorrelationIdWhenCurrentDoesNotExist() {
        // Arrange
        MDC.remove(CorrelationContext.MDC_CORRELATION_KEY);

        // Act
        String result = CorrelationContext.currentCorrelationIdOrGenerate();

        // Assert
        assertThat(result).isNotBlank();
        assertThat(UUID.fromString(result).toString()).isEqualTo(result);
    }

    @Test
    @DisplayName("Deve retornar nulo quando não existir identificador de causalidade no MDC")
    void shouldReturnNullWhenCausationIdDoesNotExist() {
        // Arrange
        MDC.remove(CorrelationContext.MDC_CAUSATION_KEY);

        // Act
        UUID result = CorrelationContext.currentCausationId();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve retornar UUID quando existir identificador de causalidade válido no MDC")
    void shouldReturnCausationIdWhenValidUuidExists() {
        // Arrange
        UUID causationId = UUID.randomUUID();
        MDC.put(
                CorrelationContext.MDC_CAUSATION_KEY,
                causationId.toString()
        );

        // Act
        UUID result = CorrelationContext.currentCausationId();

        // Assert
        assertThat(result).isEqualTo(causationId);
    }

    @Test
    @DisplayName("Deve retornar nulo quando o identificador de causalidade possuir formato inválido")
    void shouldReturnNullWhenCausationIdIsInvalid() {
        // Arrange
        MDC.put(
                CorrelationContext.MDC_CAUSATION_KEY,
                "invalid-uuid"
        );

        // Act
        UUID result = CorrelationContext.currentCausationId();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve registrar correlação e causalidade no MDC")
    void shouldPutCorrelationAndCausationIdsIntoMdc() {
        // Arrange
        String correlationId = "correlation-123";
        UUID causationId = UUID.randomUUID();

        // Act
        CorrelationContext.put(correlationId, causationId);

        // Assert
        assertThat(MDC.get(CorrelationContext.MDC_CORRELATION_KEY))
                .isEqualTo(correlationId);

        assertThat(MDC.get(CorrelationContext.MDC_CAUSATION_KEY))
                .isEqualTo(causationId.toString());
    }

    @Test
    @DisplayName("Deve registrar somente correlação quando causalidade for nula")
    void shouldPutOnlyCorrelationIdWhenCausationIdIsNull() {
        // Arrange
        String correlationId = "correlation-123";
        MDC.remove(CorrelationContext.MDC_CAUSATION_KEY);

        // Act
        CorrelationContext.put(correlationId, null);

        // Assert
        assertThat(MDC.get(CorrelationContext.MDC_CORRELATION_KEY))
                .isEqualTo(correlationId);

        assertThat(MDC.get(CorrelationContext.MDC_CAUSATION_KEY))
                .isNull();
    }

    @Test
    @DisplayName("Deve remover os identificadores de correlação e causalidade do MDC")
    void shouldClearCorrelationAndCausationIdsFromMdc() {
        // Arrange
        MDC.put(
                CorrelationContext.MDC_CORRELATION_KEY,
                "correlation-123"
        );
        MDC.put(
                CorrelationContext.MDC_CAUSATION_KEY,
                UUID.randomUUID().toString()
        );

        // Act
        CorrelationContext.clear();

        // Assert
        assertThat(MDC.get(CorrelationContext.MDC_CORRELATION_KEY))
                .isNull();

        assertThat(MDC.get(CorrelationContext.MDC_CAUSATION_KEY))
                .isNull();
    }
}