package br.com.ecofy.ms_notification.adapters.out.external;

import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;
import br.com.ecofy.ms_notification.core.port.out.EmailSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class EmailProviderAdapter implements EmailSenderPort {

    @Override
    public SendResult sendEmail(ChannelAddress to, String subject, String body) {
        log.info("[EmailProviderAdapter] SEND to={} subject={}", to.address(), subject);
        // placeholder: integra depois com SES/SendGrid/Mailgun etc.
        return new SendResult("email-stub", UUID.randomUUID().toString());
    }
}