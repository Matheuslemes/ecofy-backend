package br.com.ecofy.ms_budgeting.core.port.out;

import java.time.Duration;

public interface IdempotencyPort {

    boolean tryAcquire(String key, Duration ttl, String scope);

}
