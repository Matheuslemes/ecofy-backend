package br.com.ecofy.ms_insights.adapters.in.web.advice;

import br.com.ecofy.ms_insights.core.domain.exception.BusinessValidationException;
import br.com.ecofy.ms_insights.core.domain.exception.GoalNotFoundException;
import br.com.ecofy.ms_insights.core.domain.exception.IdempotencyViolationException;
import br.com.ecofy.ms_insights.core.domain.exception.InsightNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(GoalNotFoundException.class)
    ResponseEntity<ApiErrorResponse> goalNotFound(GoalNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of("reason", "GOAL_NOT_FOUND"));
    }

    @ExceptionHandler(InsightNotFoundException.class)
    ResponseEntity<ApiErrorResponse> insightNotFound(InsightNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of("reason", "INSIGHT_NOT_FOUND"));
    }

    @ExceptionHandler(IdempotencyViolationException.class)
    ResponseEntity<ApiErrorResponse> idem(IdempotencyViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of("reason", "IDEMPOTENCY_VIOLATION"));
    }

    @ExceptionHandler(BusinessValidationException.class)
    ResponseEntity<ApiErrorResponse> business(BusinessValidationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, Map.of("reason", "BUSINESS_VALIDATION"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        details.put("fields", fields);
        return build(HttpStatus.BAD_REQUEST, "Invalid payload", req, details);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> generic(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest req, Map<String, Object> details) {
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
