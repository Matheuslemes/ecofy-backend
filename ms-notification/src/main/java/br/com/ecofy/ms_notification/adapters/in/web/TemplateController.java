package br.com.ecofy.ms_notification.adapters.in.web;

import br.com.ecofy.ms_notification.adapters.in.web.dto.SendNotificationRequest;
import br.com.ecofy.ms_notification.adapters.in.web.dto.TemplatePreviewResponse;
import br.com.ecofy.ms_notification.adapters.in.web.dto.TemplateRequest;
import br.com.ecofy.ms_notification.adapters.in.web.dto.TemplateResponse;
import br.com.ecofy.ms_notification.adapters.out.persistence.mongo.NotificationTemplateMongoAdapter;
import br.com.ecofy.ms_notification.core.application.command.PreviewTemplateCommand;
import br.com.ecofy.ms_notification.core.domain.NotificationTemplate;
import br.com.ecofy.ms_notification.core.domain.valueobject.TemplateId;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import br.com.ecofy.ms_notification.core.port.in.PreviewTemplateUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/api/notification/v1/templates", produces = MediaType.APPLICATION_JSON_VALUE)
public class TemplateController {

    private final NotificationTemplateMongoAdapter templateAdapter;
    private final PreviewTemplateUseCase previewUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplateResponse> create(@Valid @RequestBody TemplateRequest req) {

        var now = Instant.now();

        var template = NotificationTemplate.builder()
                .id(TemplateId.newId())
                .ownerUserId(req.ownerUserId() == null ? null : new UserId(req.ownerUserId()))
                .eventType(req.eventType())
                .channel(req.channel())
                .engine(req.engine())
                .subjectTemplate(req.subjectTemplate())
                .bodyTemplate(req.bodyTemplate())
                .active(req.active())
                .createdAt(now)
                .updatedAt(now)
                .build()
                .validate();

        var saved = templateAdapter.save(template);

        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> get(@PathVariable UUID id) {
        return templateAdapter.loadById(new TemplateId(id))
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplatePreviewResponse> preview(@Valid @RequestBody SendNotificationRequest req) {

        var result = previewUseCase.preview(new PreviewTemplateCommand(
                req.userId(),
                req.eventType(),
                req.channel(),
                req.payload()
        ));

        return ResponseEntity.ok(new TemplatePreviewResponse(result.subject(), result.body()));
    }

    private static TemplateResponse toResponse(NotificationTemplate t) {
        return new TemplateResponse(
                t.getId().value(),
                t.getOwnerUserId() == null ? null : t.getOwnerUserId().value(),
                t.getEventType(),
                t.getChannel(),
                t.getEngine(),
                t.getSubjectTemplate(),
                t.getBodyTemplate(),
                t.isActive(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}