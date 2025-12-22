package br.com.ecofy.ms_categorization.core.port.out;

import java.time.Instant;

public interface IdempotencyPortOut {

    boolean tryAcquire(String key, Instant now);

}
