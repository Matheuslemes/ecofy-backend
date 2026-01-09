package br.com.ecofy.ms_notification.core.port.in;


import br.com.ecofy.ms_notification.core.application.command.PreviewTemplateCommand;
import br.com.ecofy.ms_notification.core.application.result.TemplatePreviewResult;

public interface PreviewTemplateUseCase {
    TemplatePreviewResult preview(PreviewTemplateCommand command);
}