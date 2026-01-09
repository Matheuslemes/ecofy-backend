package br.com.ecofy.ms_notification.core.port.out;

import br.com.ecofy.ms_notification.core.domain.valueobject.IdempotencyKey;

public interface IdempotencyPort {
    boolean tryAcquire(IdempotencyKey key);
}