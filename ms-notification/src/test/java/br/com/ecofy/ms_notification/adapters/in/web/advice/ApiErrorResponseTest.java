package br.com.ecofy.ms_notification.adapters.in.web.advice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Testes unitários de ApiErrorResponse")
class ApiErrorResponseTest {

    private static final String CORRELATION_ID_KEY = "correlationId";

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Deve criar resposta com traceId do MDC quando o identificador estiver disponível")
    void deveCriarRespostaComTraceIdDoMdcQuandoIdentificadorEstiverDisponivel() {
        // Arrange
        MDC.put(CORRELATION_ID_KEY, "correlation-123");

        // Act
        var response = ApiErrorResponse.of(
                "VALIDATION_ERROR",
                "Dados inválidos",
                "/api/v1/notifications"
        );

        // Assert
        assertAll(
                () -> assertEquals(
                        "VALIDATION_ERROR",
                        response.errorCode()
                ),
                () -> assertEquals(
                        "Dados inválidos",
                        response.message()
                ),
                () -> assertNotNull(response.timestamp()),
                () -> assertEquals(
                        "/api/v1/notifications",
                        response.path()
                ),
                () -> assertEquals(
                        "correlation-123",
                        response.traceId()
                )
        );
    }

    @Test
    @DisplayName("Deve criar resposta sem traceId quando o identificador não existir no MDC")
    void deveCriarRespostaSemTraceIdQuandoIdentificadorNaoExistirNoMdc() {
        // Arrange

        // Act
        var response = ApiErrorResponse.of(
                "INTERNAL_ERROR",
                "Erro interno",
                "/api/v1/notifications"
        );

        // Assert
        assertAll(
                () -> assertEquals(
                        "INTERNAL_ERROR",
                        response.errorCode()
                ),
                () -> assertEquals(
                        "Erro interno",
                        response.message()
                ),
                () -> assertNotNull(response.timestamp()),
                () -> assertEquals(
                        "/api/v1/notifications",
                        response.path()
                ),
                () -> assertNull(response.traceId())
        );
    }

    @ParameterizedTest(
            name = "[{index}] Deve ignorar correlationId inválido: \"{0}\""
    )
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("Deve criar resposta sem traceId quando o identificador do MDC estiver vazio ou em branco")
    void deveCriarRespostaSemTraceIdQuandoIdentificadorDoMdcEstiverVazioOuEmBranco(
            String correlationId
    ) {
        // Arrange
        MDC.put(CORRELATION_ID_KEY, correlationId);

        // Act
        var response = ApiErrorResponse.of(
                "NOT_FOUND",
                "Recurso não encontrado",
                "/api/v1/notifications/123"
        );

        // Assert
        assertAll(
                () -> assertEquals(
                        "NOT_FOUND",
                        response.errorCode()
                ),
                () -> assertEquals(
                        "Recurso não encontrado",
                        response.message()
                ),
                () -> assertNotNull(response.timestamp()),
                () -> assertEquals(
                        "/api/v1/notifications/123",
                        response.path()
                ),
                () -> assertNull(response.traceId())
        );
    }

    @Test
    @DisplayName("Deve utilizar o traceId informado quando a fábrica explícita for chamada")
    void deveUtilizarTraceIdInformadoQuandoFabricaExplicitaForChamada() {
        // Arrange
        MDC.put(CORRELATION_ID_KEY, "correlation-from-mdc");

        // Act
        var response = ApiErrorResponse.of(
                "BUSINESS_ERROR",
                "Operação não permitida",
                "/api/v1/notifications",
                "trace-explicit-456"
        );

        // Assert
        assertAll(
                () -> assertEquals(
                        "BUSINESS_ERROR",
                        response.errorCode()
                ),
                () -> assertEquals(
                        "Operação não permitida",
                        response.message()
                ),
                () -> assertNotNull(response.timestamp()),
                () -> assertEquals(
                        "/api/v1/notifications",
                        response.path()
                ),
                () -> assertEquals(
                        "trace-explicit-456",
                        response.traceId()
                )
        );
    }

    @Test
    @DisplayName("Deve aceitar valores nulos quando a fábrica explícita for chamada sem validações")
    void deveAceitarValoresNulosQuandoFabricaExplicitaForChamadaSemValidacoes() {
        // Arrange

        // Act
        var response = ApiErrorResponse.of(
                null,
                null,
                null,
                null
        );

        // Assert
        assertAll(
                () -> assertNull(response.errorCode()),
                () -> assertNull(response.message()),
                () -> assertNotNull(response.timestamp()),
                () -> assertNull(response.path()),
                () -> assertNull(response.traceId())
        );
    }
}