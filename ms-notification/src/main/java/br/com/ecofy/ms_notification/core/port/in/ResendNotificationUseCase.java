package br.com.ecofy.ms_notification.core.port.in;

import br.com.ecofy.ms_notification.core.application.command.ResendNotificationCommand;

import javax.management.remote.NotificationResult;

public interface ResendNotificationUseCase {
    NotificationResult resend(ResendNotificationCommand command);
}