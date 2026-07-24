package br.com.ecofy.ms_budgeting.adapters.in.web.advice;

import br.com.ecofy.ms_budgeting.adapters.correlation.CorrelationContext;
import br.com.ecofy.ms_budgeting.core.application.exception.BudgetingApplicationException;
import br.com.ecofy.ms_budgeting.core.application.exception.BudgetingValidationException;
import br.com.ecofy.ms_budgeting.core.application.exception.PaginationParameterInvalidException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetAccessForbiddenException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetAlreadyExistsException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetNotFoundException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BusinessValidationException;
import br.com.ecofy.ms_budgeting.core.domain.exception.IdempotencyViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
// Centraliza o tratamento de exceções no contrato padronizado de erros HTTP.
public class RestExceptionHandler {

    @ExceptionHandler(BudgetingValidationException.class)
        // Converte falhas de validação da aplicação em respostas HTTP 400.
    ResponseEntity<ApiErrorResponse> handleAppValidation(
            BudgetingValidationException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), req);
    }

    @ExceptionHandler(BudgetingApplicationException.class)
        // Converte falhas internas da aplicação em respostas HTTP 500.
    ResponseEntity<ApiErrorResponse> handleApplication(
            BudgetingApplicationException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode(), "Processing error", req);
    }

    @ExceptionHandler(BudgetNotFoundException.class)
        // Converte a ausência do orçamento em uma resposta HTTP 404.
    ResponseEntity<ApiErrorResponse> handleNotFound(
            BudgetNotFoundException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.NOT_FOUND, "BUDGET_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(BudgetAlreadyExistsException.class)
        // Converte conflitos de orçamento existente em respostas HTTP 409.
    ResponseEntity<ApiErrorResponse> handleConflict(
            BudgetAlreadyExistsException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.CONFLICT, "BUDGET_ALREADY_EXISTS", ex.getMessage(), req);
    }

    @ExceptionHandler(IdempotencyViolationException.class)
        // Converte violações de idempotência em respostas HTTP 409.
    ResponseEntity<ApiErrorResponse> handleIdempotency(
            IdempotencyViolationException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_EVENT", ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessValidationException.class)
        // Converte violações de regras de negócio em respostas HTTP 400.
    ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessValidationException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(BudgetAccessForbiddenException.class)
        // Converte acessos não autorizados ao orçamento em respostas HTTP 403.
    ResponseEntity<ApiErrorResponse> handleForbidden(
            BudgetAccessForbiddenException ex,
            HttpServletRequest req
    ) {
        return build(HttpStatus.FORBIDDEN, "BUDGET_ACCESS_FORBIDDEN", "Access denied", req);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
        // Converte conflitos de atualização concorrente em respostas HTTP 409.
    ResponseEntity<ApiErrorResponse> handleConcurrentUpdate(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.CONFLICT,
                "BUDGET_CONCURRENT_UPDATE",
                "O orçamento foi alterado por outra operação. Atualize os dados e tente novamente.",
                req
        );
    }

    @ExceptionHandler(PaginationParameterInvalidException.class)
        // Converte parâmetros inválidos de paginação em respostas HTTP 400.
    ResponseEntity<ApiErrorResponse> handlePagination(
            PaginationParameterInvalidException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                "PAGINATION_PARAMETER_INVALID",
                ex.getMessage(),
                req
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
        // Converte falhas de validação do payload em respostas HTTP 400.
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        Map<String, String> fields = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid payload", req, Map.of("fields", fields));
    }

    @ExceptionHandler(Exception.class)
        // Centraliza exceções não tratadas em uma resposta HTTP 500 segura.
    ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro interno ao processar a solicitação.",
                req
        );
    }

    // Constrói a resposta de erro sem detalhes adicionais.
    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest req
    ) {
        return build(status, errorCode, message, req, Map.of());
    }

    // Constrói a resposta de erro com o código de negócio no topo e os
    // dados de rastreamento disponíveis. `details` carrega apenas informações extras
    // (ex.: erros por campo) — o código de negócio agora vai em `errorCode`.
    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest req,
            Map<String, Object> details
    ) {
        String traceId = req.getHeader("X-Trace-Id");

        if (!StringUtils.hasText(traceId)) {
            traceId = req.getHeader(CorrelationContext.HEADER);
        }

        if (!StringUtils.hasText(traceId)) {
            traceId = CorrelationContext.currentCorrelationId();
        }

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                req.getRequestURI(),
                traceId,
                details
        );

        return ResponseEntity.status(status).body(body);
    }
}
