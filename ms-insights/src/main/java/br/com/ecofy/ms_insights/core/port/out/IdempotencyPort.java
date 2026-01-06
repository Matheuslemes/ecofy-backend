package br.com.ecofy.ms_insights.core.port.out;

public interface IdempotencyPort {
    boolean tryAcquire(String key, int ttlSeconds);
}
