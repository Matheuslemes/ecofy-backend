package br.com.ecofy.ms_notification.adapters.in.web.advice;

import br.com.ecofy.ms_notification.core.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTemplateNotFound(TemplateNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(NOT_FOUND).body(ApiErrorResponse.of("TEMPLATE_NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFound(NotificationNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(NOT_FOUND).body(ApiErrorResponse.of("NOTIFICATION_NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IdempotencyViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotency(IdempotencyViolationException ex, HttpServletRequest req) {
        return ResponseEntity.status(CONFLICT).body(ApiErrorResponse.of("IDEMPOTENCY_VIOLATION", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessValidationException ex, HttpServletRequest req) {
        return ResponseEntity.status(BAD_REQUEST).body(ApiErrorResponse.of("BUSINESS_VALIDATION", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(DeliveryProviderException.class)
    public ResponseEntity<ApiErrorResponse> handleProvider(DeliveryProviderException ex, HttpServletRequest req) {
        return ResponseEntity.status(BAD_GATEWAY).body(ApiErrorResponse.of("DELIVERY_PROVIDER_ERROR", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ApiErrorResponse.of("INTERNAL_ERROR", ex.getMessage(), req.getRequestURI()));
    }
}