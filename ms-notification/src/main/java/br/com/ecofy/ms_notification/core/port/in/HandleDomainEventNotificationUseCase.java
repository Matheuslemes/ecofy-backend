package br.com.ecofy.ms_notification.core.port.in;

import br.com.ecofy.ms_notification.core.application.command.HandleDomainEventCommand;

public interface HandleDomainEventNotificationUseCase {
    void handle(HandleDomainEventCommand command);
}
