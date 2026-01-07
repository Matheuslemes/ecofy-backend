package br.com.ecofy.ms_notification.core.domain.exception;


public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) { super(message); }
}