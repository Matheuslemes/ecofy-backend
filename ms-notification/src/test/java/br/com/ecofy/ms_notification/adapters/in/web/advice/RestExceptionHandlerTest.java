package br.com.ecofy.ms_notification.adapters.in.web.advice;

import br.com.ecofy.ms_notification.core.domain.exception.BusinessValidationException;
import br.com.ecofy.ms_notification.core.domain.exception.DeliveryProviderException;
import br.com.ecofy.ms_notification.core.domain.exception.IdempotencyViolationException;
import br.com.ecofy.ms_notification.core.domain.exception.NotificationNotFoundException;
import br.com.ecofy.ms_notification.core.domain.exception.TemplateNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de RestExceptionHandler")
class RestExceptionHandlerTest {

    private static final String REQUEST_PATH =
            "/api/v1/notifications/notification-123";

    private static final String TRACE_ID = "correlation-123";

    @Mock
    private HttpServletRequest request;

    @Mock
    private TemplateNotFoundException templateNotFoundException;

    @Mock
    private NotificationNotFoundException notificationNotFoundException;

    @Mock
    private IdempotencyViolationException idempotencyViolationException;

    @Mock
    private BusinessValidationException businessValidationException;

    @Mock
    private DeliveryProviderException deliveryProviderException;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private HttpMessageNotReadableException httpMessageNotReadableException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Exception genericException;

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();

        MDC.clear();
        MDC.put("correlationId", TRACE_ID);

        when(request.getRequestURI()).thenReturn(REQUEST_PATH);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Deve retornar não encontrado quando o template não existir")
    void deveRetornarNaoEncontradoQuandoTemplateNaoExistir() {
        // Arrange
        when(templateNotFoundException.getMessage())
                .thenReturn("Template não encontrado");

        // Act
        var response = handler.handleTemplateNotFound(
                templateNotFoundException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "TEMPLATE_NOT_FOUND",
                "Template não encontrado"
        );
    }

    @Test
    @DisplayName("Deve retornar não encontrado quando a notificação não existir")
    void deveRetornarNaoEncontradoQuandoNotificacaoNaoExistir() {
        // Arrange
        when(notificationNotFoundException.getMessage())
                .thenReturn("Notificação não encontrada");

        // Act
        var response = handler.handleNotificationNotFound(
                notificationNotFoundException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "NOTIFICATION_NOT_FOUND",
                "Notificação não encontrada"
        );
    }

    @Test
    @DisplayName("Deve retornar conflito quando ocorrer violação de idempotência")
    void deveRetornarConflitoQuandoOcorrerViolacaoDeIdempotencia() {
        // Arrange
        when(idempotencyViolationException.getMessage())
                .thenReturn("Evento já processado");

        // Act
        var response = handler.handleIdempotency(
                idempotencyViolationException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                "IDEMPOTENCY_VIOLATION",
                "Evento já processado"
        );
    }

    @Test
    @DisplayName("Deve retornar requisição inválida quando ocorrer violação de negócio")
    void deveRetornarRequisicaoInvalidaQuandoOcorrerViolacaoDeNegocio() {
        // Arrange
        when(businessValidationException.getMessage())
                .thenReturn("Canal de notificação inválido");

        // Act
        var response = handler.handleBusiness(
                businessValidationException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "BUSINESS_VALIDATION",
                "Canal de notificação inválido"
        );
    }

    @Test
    @DisplayName("Deve retornar gateway inválido quando o provedor externo falhar")
    void deveRetornarGatewayInvalidoQuandoProvedorExternoFalhar() {
        // Arrange
        when(deliveryProviderException.getMessage())
                .thenReturn("Provedor indisponível");

        // Act
        var response = handler.handleProvider(
                deliveryProviderException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_GATEWAY,
                "DELIVERY_PROVIDER_ERROR",
                "Provedor indisponível"
        );
    }

    @Test
    @DisplayName("Deve retornar o primeiro erro de campo quando a validação do corpo falhar")
    void deveRetornarPrimeiroErroDeCampoQuandoValidacaoDoCorpoFalhar() {
        // Arrange
        var firstError = new FieldError(
                "request",
                "recipient",
                "não deve estar em branco"
        );

        var secondError = new FieldError(
                "request",
                "channel",
                "deve ser informado"
        );

        when(methodArgumentNotValidException.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(firstError, secondError));

        // Act
        var response = handler.handleBodyValidation(
                methodArgumentNotValidException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "recipient: não deve estar em branco"
        );
    }

    @Test
    @DisplayName("Deve retornar mensagem padrão quando não existirem erros de campo")
    void deveRetornarMensagemPadraoQuandoNaoExistiremErrosDeCampo() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of());

        // Act
        var response = handler.handleBodyValidation(
                methodArgumentNotValidException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Requisição inválida"
        );
    }

    @Test
    @DisplayName("Deve retornar requisição inválida quando ocorrer violação de restrição")
    void deveRetornarRequisicaoInvalidaQuandoOcorrerViolacaoDeRestricao() {
        // Arrange
        when(constraintViolationException.getMessage())
                .thenReturn("userId: não deve ser nulo");

        // Act
        var response = handler.handleConstraintViolation(
                constraintViolationException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "userId: não deve ser nulo"
        );
    }

    @Test
    @DisplayName("Deve retornar requisição malformada quando o corpo não puder ser lido")
    void deveRetornarRequisicaoMalformadaQuandoCorpoNaoPuderSerLido() {
        // Arrange

        // Act
        var response = handler.handleUnreadable(
                httpMessageNotReadableException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Corpo da requisição inválido ou malformado."
        );
    }

    @Test
    @DisplayName("Deve retornar erro interno seguro quando ocorrer exceção não tratada")
    void deveRetornarErroInternoSeguroQuandoOcorrerExcecaoNaoTratada() {
        // Arrange
        when(genericException.getMessage())
                .thenReturn("Senha interna do provedor: secret-123");

        // Act
        var response = handler.handleGeneric(
                genericException,
                request
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Erro interno inesperado ao processar a notificação."
        );

        var body = response.getBody();

        assertNotNull(body);
        assertFalse(body.message().contains("secret-123"));
    }

    private static void assertErrorResponse(
            ResponseEntity<ApiErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedErrorCode,
            String expectedMessage
    ) {
        var body = response.getBody();

        assertAll(
                () -> assertEquals(
                        expectedStatus,
                        response.getStatusCode()
                ),
                () -> assertNotNull(body),
                () -> assertEquals(
                        expectedErrorCode,
                        body.errorCode()
                ),
                () -> assertEquals(
                        expectedMessage,
                        body.message()
                ),
                () -> assertNotNull(body.timestamp()),
                () -> assertEquals(
                        REQUEST_PATH,
                        body.path()
                ),
                () -> assertEquals(
                        TRACE_ID,
                        body.traceId()
                )
        );
    }
}