package br.com.ecofy.ms_insights.adapters.in.web.advice;

import br.com.ecofy.ms_insights.core.domain.exception.BusinessValidationException;
import br.com.ecofy.ms_insights.core.domain.exception.ExternalDataUnavailableException;
import br.com.ecofy.ms_insights.core.domain.exception.GoalNotFoundException;
import br.com.ecofy.ms_insights.core.domain.exception.IdempotencyViolationException;
import br.com.ecofy.ms_insights.core.domain.exception.InsightNotFoundException;
import br.com.ecofy.ms_insights.core.domain.exception.RebuildRunNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// Centraliza o tratamento de erros expostos pela API de insights.
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    // Converte a ausência de uma meta em resposta HTTP 404.
    @ExceptionHandler(GoalNotFoundException.class)
    ResponseEntity<ApiErrorResponse> goalNotFound(GoalNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "GOAL_NOT_FOUND", ex.getMessage(), req);
    }

    // Converte a ausência de um insight em resposta HTTP 404.
    @ExceptionHandler(InsightNotFoundException.class)
    ResponseEntity<ApiErrorResponse> insightNotFound(InsightNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "INSIGHT_NOT_FOUND", ex.getMessage(), req);
    }

    // Converte a ausência de uma execução de rebuild em resposta HTTP 404.
    @ExceptionHandler(RebuildRunNotFoundException.class)
    ResponseEntity<ApiErrorResponse> rebuildNotFound(RebuildRunNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "REBUILD_RUN_NOT_FOUND", ex.getMessage(), req);
    }

    // Converte violações de idempotência em resposta HTTP 409.
    @ExceptionHandler(IdempotencyViolationException.class)
    ResponseEntity<ApiErrorResponse> idem(IdempotencyViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "IDEMPOTENCY_VIOLATION", ex.getMessage(), req);
    }

    // Converte violações de negócio em resposta HTTP 400.
    @ExceptionHandler(BusinessValidationException.class)
    ResponseEntity<ApiErrorResponse> business(BusinessValidationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BUSINESS_VALIDATION", ex.getMessage(), req);
    }

    // Detalha violações de campos em uma resposta HTTP 400.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid payload", req, Map.of("fields", fields));
    }

    // Converte violações de parâmetros em resposta HTTP 400.
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", ex.getMessage(), req);
    }

    // Converte corpos ilegíveis em resposta HTTP 400.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> unreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed or unreadable request body", req);
    }

    // Converte argumentos inválidos em resposta HTTP 400.
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> illegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), req);
    }

    // Converte indisponibilidade externa em resposta HTTP 503.
    @ExceptionHandler(ExternalDataUnavailableException.class)
    ResponseEntity<ApiErrorResponse> externalUnavailable(ExternalDataUnavailableException ex, HttpServletRequest req) {
        log.warn("[RestExceptionHandler] external data unavailable source={} message={}", ex.getSource(), ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_DATA_UNAVAILABLE",
                "External dependency unavailable: " + ex.getSource(), req,
                Map.of("source", ex.getSource()));
    }

    // Converte falhas inesperadas em uma resposta genérica HTTP 500.
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> generic(Exception ex, HttpServletRequest req) {
        log.error("[RestExceptionHandler] unexpected error path={} type={} message={}",
                req.getRequestURI(), ex.getClass().getName(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro interno ao processar a solicitação.", req);
    }

    // Constrói a resposta de erro sem detalhes adicionais.
    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String errorCode, String message, HttpServletRequest req) {
        return build(status, errorCode, message, req, Map.of());
    }

    // Centraliza a construção das respostas de erro com o código de negócio no topo
    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String errorCode, String message, HttpServletRequest req, Map<String, Object> details) {
        String traceId = req.getHeader("X-Trace-Id");
        if (!StringUtils.hasText(traceId)) traceId = req.getHeader("X-Correlation-Id");

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
