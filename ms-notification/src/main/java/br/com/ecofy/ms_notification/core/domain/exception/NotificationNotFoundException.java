package br.com.ecofy.ms_notification.core.domain.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) { super(message); }
}