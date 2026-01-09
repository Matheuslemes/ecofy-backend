package br.com.ecofy.ms_notification.core.port.out;


import br.com.ecofy.ms_notification.core.domain.valueobject.ChannelAddress;

public interface WhatsAppSenderPort {
    SendResult sendWhatsApp(ChannelAddress to, String body);

    record SendResult(String provider, String providerMessageId) {}
}
