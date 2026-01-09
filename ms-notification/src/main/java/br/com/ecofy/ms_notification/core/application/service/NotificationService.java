package br.com.ecofy.ms_notification.core.application.service;

import br.com.ecofy.ms_notification.config.NotificationProperties;
import br.com.ecofy.ms_notification.core.application.command.ResendNotificationCommand;
import br.com.ecofy.ms_notification.core.application.command.SendNotificationCommand;
import br.com.ecofy.ms_notification.core.domain.DeliveryAttempt;
import br.com.ecofy.ms_notification.core.domain.Notification;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;
import br.com.ecofy.ms_notification.core.domain.enums.NotificationStatus;
import br.com.ecofy.ms_notification.core.domain.exception.*;
import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;
import br.com.ecofy.ms_notification.core.domain.valueobject.IdempotencyKey;
import br.com.ecofy.ms_notification.core.domain.valueobject.NotificationId;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import br.com.ecofy.ms_notification.core.port.in.ResendNotificationUseCase;
import br.com.ecofy.ms_notification.core.port.in.SendNotificationUseCase;
import br.com.ecofy.ms_notification.core.port.out.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.management.remote.NotificationResult;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService implements SendNotificationUseCase, ResendNotificationUseCase {

    private final NotificationProperties props;

    private final LoadNotificationTemplatePort loadTemplatePort;
    private final LoadUserContactInfoPort loadUserContactInfoPort;

    private final SaveNotificationPort saveNotificationPort;
    private final SaveDeliveryAttemptPort saveDeliveryAttemptPort;

    private final EmailSenderPort emailSenderPort;
    private final WhatsAppSenderPort whatsAppSenderPort;
    private final PushSenderPort pushSenderPort;

    private final IdempotencyPort idempotencyPort;
    private final PublishNotificationEventPort publishNotificationEventPort; // opcional

    public NotificationService(NotificationProperties props,
                               LoadNotificationTemplatePort loadTemplatePort,
                               LoadUserContactInfoPort loadUserContactInfoPort,
                               SaveNotificationPort saveNotificationPort,
                               SaveDeliveryAttemptPort saveDeliveryAttemptPort,
                               EmailSenderPort emailSenderPort,
                               WhatsAppSenderPort whatsAppSenderPort,
                               PushSenderPort pushSenderPort,
                               IdempotencyPort idempotencyPort,
                               Optional<PublishNotificationEventPort> publishNotificationEventPort) {

        this.props = Objects.requireNonNull(props, "props must not be null");
        this.loadTemplatePort = Objects.requireNonNull(loadTemplatePort, "loadTemplatePort must not be null");
        this.loadUserContactInfoPort = Objects.requireNonNull(loadUserContactInfoPort, "loadUserContactInfoPort must not be null");
        this.saveNotificationPort = Objects.requireNonNull(saveNotificationPort, "saveNotificationPort must not be null");
        this.saveDeliveryAttemptPort = Objects.requireNonNull(saveDeliveryAttemptPort, "saveDeliveryAttemptPort must not be null");
        this.emailSenderPort = Objects.requireNonNull(emailSenderPort, "emailSenderPort must not be null");
        this.whatsAppSenderPort = Objects.requireNonNull(whatsAppSenderPort, "whatsAppSenderPort must not be null");
        this.pushSenderPort = Objects.requireNonNull(pushSenderPort, "pushSenderPort must not be null");
        this.idempotencyPort = Objects.requireNonNull(idempotencyPort, "idempotencyPort must not be null");
        this.publishNotificationEventPort = publishNotificationEventPort.orElse(null);
    }

    @Override
    public NotificationResult send(SendNotificationCommand command) {

        if (props.getIdempotency().isEnabled()) {
            var key = new IdempotencyKey(resolveIdempotencyKey(command.idempotencyKey(), "send"));
            boolean ok = idempotencyPort.tryAcquire(key);
            if (!ok) throw new IdempotencyViolationException("Idempotency key already used: " + key.value());
        }

        UUID userIdRaw = Objects.requireNonNull(command.userId(), "userId must not be null");
        var userId = new UserId(userIdRaw);

        NotificationChannel channel = Objects.requireNonNull(command.channel(), "channel must not be null");
        Map<String, Object> payload = command.payload() == null ? Map.of() : command.payload();

        var template = loadTemplatePort.loadActiveTemplate(userId, command.eventType(), channel)
                .orElseThrow(() -> new TemplateNotFoundException(
                        "No active template found for eventType=%s channel=%s".formatted(command.eventType(), channel)
                ));

        String destination = resolveDestination(command.destinationOverride(), channel, userId);
        var dest = new ChannelAddress(channel, destination);

        String subject = (channel == NotificationChannel.EMAIL)
                ? template.renderSubject(payload)
                : null;

        String body = template.renderBody(payload);

        var now = Instant.now();

        var notification = Notification.builder()
                .id(NotificationId.newId())
                .userId(userId)
                .eventType(command.eventType())
                .channel(channel)
                .destination(dest)
                .subject(subject)
                .body(body)
                .status(NotificationStatus.PENDING)
                .attemptCount(0)
                .idempotencyKey(command.idempotencyKey() == null ? null : new IdempotencyKey(command.idempotencyKey()))
                .payload(payload)
                .createdAt(now)
                .updatedAt(now)
                .build()
                .validateForSend();

        Notification saved = saveNotificationPort.save(notification);

        Notification delivered = attemptDelivery(saved);

        return toResult(delivered);
    }

    @Override
    public NotificationResult resend(ResendNotificationCommand command) {

        if (props.getIdempotency().isEnabled()) {
            var key = new IdempotencyKey(resolveIdempotencyKey(command.idempotencyKey(), "resend"));
            boolean ok = idempotencyPort.tryAcquire(key);
            if (!ok) throw new IdempotencyViolationException("Idempotency key already used: " + key.value());
        }

        var notificationId = new NotificationId(Objects.requireNonNull(command.notificationId(), "notificationId must not be null"));

        var current = saveNotificationPort.loadById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId.value()));

        // reset state for resend
        var reset = current.toBuilder()
                .status(NotificationStatus.PENDING)
                .updatedAt(Instant.now())
                .build();

        Notification saved = saveNotificationPort.save(reset);
        Notification delivered = attemptDelivery(saved);

        return toResult(delivered);
    }

    private Notification attemptDelivery(Notification notification) {

        int attemptNumber = notification.getAttemptCount() + 1;

        try {
            switch (notification.getChannel()) {
                case EMAIL -> {
                    var result = emailSenderPort.sendEmail(notification.getDestination(), notification.getSubject(), notification.getBody());
                    saveDeliveryAttemptPort.save(DeliveryAttempt.success(notification.getId(), notification.getChannel(), attemptNumber,
                            result.provider(), result.providerMessageId()));
                }
                case WHATSAPP -> {
                    var result = whatsAppSenderPort.sendWhatsApp(notification.getDestination(), notification.getBody());
                    saveDeliveryAttemptPort.save(DeliveryAttempt.success(notification.getId(), notification.getChannel(), attemptNumber,
                            result.provider(), result.providerMessageId()));
                }
                case PUSH -> {
                    String title = notification.getSubject() == null ? "EcoFy" : notification.getSubject();
                    var result = pushSenderPort.sendPush(notification.getDestination(), title, notification.getBody());
                    saveDeliveryAttemptPort.save(DeliveryAttempt.success(notification.getId(), notification.getChannel(), attemptNumber,
                            result.provider(), result.providerMessageId()));
                }
                default -> throw new BusinessValidationException("Unsupported channel: " + notification.getChannel());
            }

            var updated = notification.toBuilder()
                    .status(NotificationStatus.SENT)
                    .attemptCount(attemptNumber)
                    .updatedAt(Instant.now())
                    .build();

            Notification saved = saveNotificationPort.save(updated);

            if (publishNotificationEventPort != null) {
                publishNotificationEventPort.publish(
                        NotificationSentEvent.from(saved)
                );
            }

            return saved;

        } catch (RuntimeException ex) {

            log.warn("[NotificationService] delivery failed notificationId={} attempt={}: {}",
                    notification.getId().value(), attemptNumber, ex.getMessage());

            saveDeliveryAttemptPort.save(DeliveryAttempt.failure(
                    notification.getId(),
                    notification.getChannel(),
                    attemptNumber,
                    "provider",
                    "DELIVERY_FAILED",
                    ex.getMessage()
            ));

            var updated = notification.toBuilder()
                    .status(NotificationStatus.FAILED)
                    .attemptCount(attemptNumber)
                    .updatedAt(Instant.now())
                    .build();

            saveNotificationPort.save(updated);

            throw new DeliveryProviderException("Delivery failed for notificationId=" + notification.getId().value(), ex);
        }
    }

    private String resolveDestination(String override, NotificationChannel channel, UserId userId) {

        if (override != null && !override.isBlank()) {
            return override;
        }

        var contact = loadUserContactInfoPort.load(userId)
                .orElseThrow(() -> new BusinessValidationException("User contact info not found for userId=" + userId.value()));

        return switch (channel) {
            case EMAIL -> {
                if (contact.email() == null || contact.email().isBlank())
                    throw new BusinessValidationException("User has no email");
                yield contact.email();
            }
            case WHATSAPP -> {
                if (contact.phoneE164() == null || contact.phoneE164().isBlank())
                    throw new BusinessValidationException("User has no phone");
                yield contact.phoneE164();
            }
            case PUSH -> {
                if (contact.deviceToken() == null || contact.deviceToken().isBlank())
                    throw new BusinessValidationException("User has no device token");
                yield contact.deviceToken();
            }
        };
    }

    private String resolveIdempotencyKey(String provided, String suffix) {
        String base = (provided == null || provided.isBlank())
                ? UUID.randomUUID().toString()
                : provided.trim();
        return base + ":" + suffix;
    }

    private static NotificationResult toResult(Notification n) {
        return new NotificationResult(
                n.getId().value(),
                n.getUserId().value(),
                n.getEventType(),
                n.getChannel(),
                n.getDestination().address(),
                n.getSubject(),
                n.getBody(),
                n.getStatus(),
                n.getAttemptCount(),
                n.getPayload(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }
}
