package br.com.ecofy.ms_notification.core.port.in;

import br.com.ecofy.ms_notification.core.application.command.SendNotificationCommand;

import javax.management.remote.NotificationResult;

public interface SendNotificationUseCase {
    NotificationResult send(SendNotificationCommand command);
}