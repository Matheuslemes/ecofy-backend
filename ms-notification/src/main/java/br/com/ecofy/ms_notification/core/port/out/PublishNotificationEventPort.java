package br.com.ecofy.ms_notification.core.port.out;

public interface PublishNotificationEventPort {
    void publish(NotificationSentEvent event);
}
