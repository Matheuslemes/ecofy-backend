package br.com.ecofy.ms_budgeting.adapters.in.web.advice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTest {

    @Test
    @DisplayName("Deve criar resposta de erro com todos os campos preenchidos")
    void shouldCreateErrorResponseWithAllFields() {
        // Arrange
        Instant timestamp = Instant.parse("2026-08-11T19:00:00Z");
        int status = 400;
        String errorCode = "VALIDATION_ERROR";
        String message = "Dados inválidos";
        String path = "/api/v1/budgets";
        String traceId = "trace-123";
        Map<String, Object> details = Map.of(
                "field", "amount",
                "reason", "must be positive"
        );

        // Act
        ApiErrorResponse response = new ApiErrorResponse(
                timestamp,
                status,
                errorCode,
                message,
                path,
                traceId,
                details
        );

        // Assert
        assertThat(response.timestamp()).isEqualTo(timestamp);
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.errorCode()).isEqualTo(errorCode);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.path()).isEqualTo(path);
        assertThat(response.traceId()).isEqualTo(traceId);
        assertThat(response.details()).isEqualTo(details);
    }

    @Test
    @DisplayName("Deve permitir criação da resposta com campos de referência nulos")
    void shouldAllowCreationWithNullReferenceFields() {
        // Arrange

        // Act
        ApiErrorResponse response = new ApiErrorResponse(
                null,
                0,
                null,
                null,
                null,
                null,
                null
        );

        // Assert
        assertThat(response.timestamp()).isNull();
        assertThat(response.status()).isZero();
        assertThat(response.errorCode()).isNull();
        assertThat(response.message()).isNull();
        assertThat(response.path()).isNull();
        assertThat(response.traceId()).isNull();
        assertThat(response.details()).isNull();
    }

    @Test
    @DisplayName("Deve permitir mapa de detalhes vazio")
    void shouldAllowEmptyDetails() {
        // Arrange
        Map<String, Object> details = Map.of();

        // Act
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.parse("2026-08-11T19:00:00Z"),
                404,
                "NOT_FOUND",
                "Recurso não encontrado",
                "/api/v1/budgets/1",
                "trace-123",
                details
        );

        // Assert
        assertThat(response.details()).isEmpty();
    }

    @Test
    @DisplayName("Deve considerar respostas iguais quando todos os campos forem iguais")
    void shouldConsiderResponsesEqualWhenAllFieldsAreEqual() {
        // Arrange
        Instant timestamp = Instant.parse("2026-08-11T19:00:00Z");
        Map<String, Object> details = Map.of("field", "amount");

        ApiErrorResponse first = new ApiErrorResponse(
                timestamp,
                400,
                "VALIDATION_ERROR",
                "Dados inválidos",
                "/api/v1/budgets",
                "trace-123",
                details
        );

        ApiErrorResponse second = new ApiErrorResponse(
                timestamp,
                400,
                "VALIDATION_ERROR",
                "Dados inválidos",
                "/api/v1/budgets",
                "trace-123",
                details
        );

        // Act / Assert
        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
    }

    @Test
    @DisplayName("Deve considerar a mesma instância igual a ela mesma")
    void shouldConsiderSameInstanceEqual() {
        // Arrange
        ApiErrorResponse response = createResponse();

        // Act / Assert
        assertThat(response).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve considerar resposta diferente de nulo e de outro tipo")
    void shouldConsiderResponseDifferentFromNullAndOtherType() {
        // Arrange
        ApiErrorResponse response = createResponse();

        // Act / Assert
        assertThat(response).isNotEqualTo(null);
        assertThat(response).isNotEqualTo("outro-objeto");
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando o timestamp for diferente")
    void shouldConsiderResponsesDifferentWhenTimestampDiffers() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                Instant.parse("2026-08-12T19:00:00Z"),
                first.status(),
                first.errorCode(),
                first.message(),
                first.path(),
                first.traceId(),
                first.details()
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando o status for diferente")
    void shouldConsiderResponsesDifferentWhenStatusDiffers() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                first.timestamp(),
                500,
                first.errorCode(),
                first.message(),
                first.path(),
                first.traceId(),
                first.details()
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando o código de erro for diferente")
    void shouldConsiderResponsesDifferentWhenErrorCodeDiffers() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                first.timestamp(),
                first.status(),
                "INTERNAL_ERROR",
                first.message(),
                first.path(),
                first.traceId(),
                first.details()
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando a mensagem for diferente")
    void shouldConsiderResponsesDifferentWhenMessageDiffers() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                first.timestamp(),
                first.status(),
                first.errorCode(),
                "Outra mensagem",
                first.path(),
                first.traceId(),
                first.details()
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando o path for diferente")
    void shouldConsiderResponsesDifferentWhenPathDiffers() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                first.timestamp(),
                first.status(),
                first.errorCode(),
                first.message(),
                "/api/v1/other",
                first.traceId(),
                first.details()
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando o traceId for diferente")
    void shouldConsiderResponsesDifferentWhenTraceIdDiffers() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                first.timestamp(),
                first.status(),
                first.errorCode(),
                first.message(),
                first.path(),
                "trace-456",
                first.details()
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar respostas diferentes quando os detalhes forem diferentes")
    void shouldConsiderResponsesDifferentWhenDetailsDiffer() {
        // Arrange
        ApiErrorResponse first = createResponse();

        ApiErrorResponse second = new ApiErrorResponse(
                first.timestamp(),
                first.status(),
                first.errorCode(),
                first.message(),
                first.path(),
                first.traceId(),
                Map.of("field", "currency")
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve considerar iguais respostas com todos os campos de referência nulos")
    void shouldConsiderResponsesWithNullFieldsEqual() {
        // Arrange
        ApiErrorResponse first = new ApiErrorResponse(
                null,
                0,
                null,
                null,
                null,
                null,
                null
        );

        ApiErrorResponse second = new ApiErrorResponse(
                null,
                0,
                null,
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
    @DisplayName("Deve gerar hashCode consistente")
    void shouldGenerateConsistentHashCode() {
        // Arrange
        ApiErrorResponse response = createResponse();

        // Act
        int firstHashCode = response.hashCode();
        int secondHashCode = response.hashCode();

        // Assert
        assertThat(firstHashCode).isEqualTo(secondHashCode);
    }

    @Test
    @DisplayName("Deve gerar representação textual contendo todos os campos")
    void shouldGenerateToStringContainingAllFields() {
        // Arrange
        ApiErrorResponse response = createResponse();

        // Act
        String result = response.toString();

        // Assert
        assertThat(result)
                .contains("ApiErrorResponse")
                .contains("timestamp=" + response.timestamp())
                .contains("status=" + response.status())
                .contains("errorCode=" + response.errorCode())
                .contains("message=" + response.message())
                .contains("path=" + response.path())
                .contains("traceId=" + response.traceId())
                .contains("details=" + response.details());
    }

    private ApiErrorResponse createResponse() {
        return new ApiErrorResponse(
                Instant.parse("2026-08-11T19:00:00Z"),
                400,
                "VALIDATION_ERROR",
                "Dados inválidos",
                "/api/v1/budgets",
                "trace-123",
                Map.of(
                        "field", "amount",
                        "reason", "must be positive"
                )
        );
    }
}
