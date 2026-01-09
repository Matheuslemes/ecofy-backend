package br.com.ecofy.ms_notification.adapters.out.messaging;

import br.com.ecofy.ms_notification.adapters.out.messaging.dto.NotificationSentEvent;
import br.com.ecofy.ms_notification.config.NotificationProperties;
import br.com.ecofy.ms_notification.core.port.out.PublishNotificationEventPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventsKafkaAdapter implements PublishNotificationEventPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationProperties props;

    public NotificationEventsKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate, NotificationProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    @Override
    public void publish(NotificationSentEvent event) {
        String topic = props.getTopics().getNotificationSent();
        kafkaTemplate.send(topic, event.notificationId().toString(), event);
        log.debug("[NotificationEventsKafkaAdapter] published notification.sent notificationId={}", event.notificationId());
    }
}