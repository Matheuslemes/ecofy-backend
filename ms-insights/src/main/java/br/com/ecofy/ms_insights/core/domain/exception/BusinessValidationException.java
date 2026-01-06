package br.com.ecofy.ms_insights.core.domain.exception;

public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) { super(message); }
}
