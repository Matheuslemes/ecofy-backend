package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;

import java.util.Optional;

public interface LoadUserContactInfoPort {

    record UserContactInfo(
            String email,
            String phoneE164,
            String deviceToken
    ) {}

    Optional<UserContactInfo> load(UserId userId);
}