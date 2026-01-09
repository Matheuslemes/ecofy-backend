package br.com.ecofy.ms_notification.core.application.service;

import br.com.ecofy.ms_notification.core.application.command.PreviewTemplateCommand;
import br.com.ecofy.ms_notification.core.application.result.TemplatePreviewResult;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;
import br.com.ecofy.ms_notification.core.domain.exception.TemplateNotFoundException;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import br.com.ecofy.ms_notification.core.port.in.PreviewTemplateUseCase;
import br.com.ecofy.ms_notification.core.port.out.LoadNotificationTemplatePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TemplatePreviewService implements PreviewTemplateUseCase {

    private final LoadNotificationTemplatePort loadTemplatePort;

    public TemplatePreviewService(LoadNotificationTemplatePort loadTemplatePort) {
        this.loadTemplatePort = loadTemplatePort;
    }

    @Override
    public TemplatePreviewResult preview(PreviewTemplateCommand command) {

        var userId = command.userId() == null ? null : new UserId(command.userId());

        var template = loadTemplatePort.loadActiveTemplate(
                userId,
                command.eventType(),
                command.channel()
        ).orElseThrow(() -> new TemplateNotFoundException(
                "No active template found for eventType=%s channel=%s".formatted(command.eventType(), command.channel())
        ));

        var subject = template.getChannel() == NotificationChannel.EMAIL
                ? template.renderSubject(command.payload())
                : null;

        var body = template.renderBody(command.payload());

        return new TemplatePreviewResult(subject, body);
    }
}