package br.com.ecofy.ms_insights.core.domain.exception;

public class InsightNotFoundException extends RuntimeException {
    public InsightNotFoundException(String message) { super(message); }
}