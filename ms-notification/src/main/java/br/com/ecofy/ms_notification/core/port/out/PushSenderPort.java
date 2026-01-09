package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;

public interface PushSenderPort {
    SendResult sendPush(ChannelAddress to, String title, String body);

    record SendResult(String provider, String providerMessageId) {}
}
