package br.com.ecofy.ms_budgeting.adapters.in.web.advice;

import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetAlreadyExistsException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetNotFoundException;
import br.com.ecofy.ms_budgeting.core.domain.exception.BusinessValidationException;
import br.com.ecofy.ms_budgeting.core.domain.exception.IdempotencyViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BudgetNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(BudgetNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(BudgetAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(BudgetAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of("reason", "BUDGET_ALREADY_EXISTS"));
    }

    @ExceptionHandler(IdempotencyViolationException.class)
    ResponseEntity<ApiErrorResponse> handleIdempotency(IdempotencyViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of("reason", "IDEMPOTENCY_VIOLATION"));
    }

    @ExceptionHandler(BusinessValidationException.class)
    ResponseEntity<ApiErrorResponse> handleBusiness(BusinessValidationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, Map.of("reason", "BUSINESS_VALIDATION"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        details.put("fields", fields);
        return build(HttpStatus.BAD_REQUEST, "Invalid payload", req, details);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest req,
            Map<String, Object> details
    ) {
        String traceId = req.getHeader("X-Trace-Id");
        if (!StringUtils.hasText(traceId)) traceId = req.getHeader("X-Correlation-Id");

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI(),
                traceId,
                details
        );

        return ResponseEntity.status(status).body(body);
    }

}