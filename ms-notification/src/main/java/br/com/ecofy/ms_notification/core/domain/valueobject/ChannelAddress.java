package br.com.ecofy.ms_notification.core.domain.valueobject;

import br.com.ecofy.ms_notification.core.domain.enums.NotificationChannel;

import java.util.Objects;

public record ChannelAddress(NotificationChannel channel, String address) {

    public ChannelAddress {
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(address, "address must not be null");
        if (address.isBlank()) {
            throw new IllegalArgumentException("address must not be blank");
        }
    }
}