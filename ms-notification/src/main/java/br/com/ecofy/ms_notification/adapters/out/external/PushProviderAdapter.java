package br.com.ecofy.ms_notification.adapters.out.external;

import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;
import br.com.ecofy.ms_notification.core.port.out.PushSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class PushProviderAdapter implements PushSenderPort {

    @Override
    public SendResult sendPush(ChannelAddress to, String title, String body) {
        log.info("[PushProviderAdapter] PUSH to={} title={}", to.address(), title);
        return new SendResult("push-stub", UUID.randomUUID().toString());
    }
}
