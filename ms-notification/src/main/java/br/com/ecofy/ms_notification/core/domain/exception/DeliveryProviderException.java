package br.com.ecofy.ms_notification.core.domain.exception;

public class DeliveryProviderException extends RuntimeException {
    public DeliveryProviderException(String message, Throwable cause) { super(message, cause); }
    public DeliveryProviderException(String message) { super(message); }
}