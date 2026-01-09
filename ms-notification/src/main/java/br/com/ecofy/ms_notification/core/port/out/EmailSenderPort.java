package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;

public interface EmailSenderPort {
    SendResult sendEmail(ChannelAddress to, String subject, String body);

    record SendResult(String provider, String providerMessageId) {}
}