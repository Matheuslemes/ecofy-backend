package br.com.ecofy.ms_notification.adapters.in.web;

import br.com.ecofy.ms_notification.adapters.in.web.dto.NotificationResponse;
import br.com.ecofy.ms_notification.adapters.in.web.dto.ResendRequest;
import br.com.ecofy.ms_notification.adapters.in.web.dto.SendNotificationRequest;
import br.com.ecofy.ms_notification.core.application.command.ResendNotificationCommand;
import br.com.ecofy.ms_notification.core.application.command.SendNotificationCommand;
import br.com.ecofy.ms_notification.core.application.result.NotificationResult;
import br.com.ecofy.ms_notification.core.port.in.ListNotificationsUseCase;
import br.com.ecofy.ms_notification.core.port.in.ResendNotificationUseCase;
import br.com.ecofy.ms_notification.core.port.in.SendNotificationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/api/notification/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

    private final SendNotificationUseCase sendUseCase;
    private final ResendNotificationUseCase resendUseCase;
    private final ListNotificationsUseCase listUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest request) {

        var result = sendUseCase.send(new SendNotificationCommand(
                request.userId(),
                request.eventType(),
                request.channel(),
                request.destinationOverride(),
                request.payload(),
                request.idempotencyKey()
        ));

        return ResponseEntity.ok(toResponse(result));
    }

    @PostMapping(path = "/resend", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationResponse> resend(@Valid @RequestBody ResendRequest request) {

        var result = resendUseCase.resend(new ResendNotificationCommand(
                request.notificationId(),
                request.idempotencyKey()
        ));

        return ResponseEntity.ok(toResponse(result));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var list = listUseCase.listByUser(userId, Math.min(limit, 200))
                .stream().map(NotificationController::toResponse).toList();

        return ResponseEntity.ok(list);
    }

    private static NotificationResponse toResponse(NotificationResult r) {
        return new NotificationResponse(
                r.id(), r.userId(), r.eventType(), r.channel(), r.destination(),
                r.subject(), r.body(), r.status(), r.attemptCount(), r.payload(),
                r.createdAt(), r.updatedAt()
        );
    }
}
