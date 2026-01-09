package br.com.ecofy.ms_notification.adapters.out.external;

import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;
import br.com.ecofy.ms_notification.core.port.out.WhatsAppSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class WhatsAppProviderAdapter implements WhatsAppSenderPort {

    @Override
    public SendResult sendWhatsApp(ChannelAddress to, String body) {
        log.info("[WhatsAppProviderAdapter] SEND to={}", to.address());
        return new SendResult("whatsapp-stub", UUID.randomUUID().toString());
    }
}