package br.com.ecofy.gateway.api_gateway.core.domain;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

public final class RateLimitPolicy {

    public enum Strategy {
        FIXED_WINDOW,
        SLIDING_WINDOW,
        TOKEN_BUCKET
    }

    private final String id;

    private final Strategy strategy;

    private final long capacity;

    private final long refillTokens;

    private final Duration refillPeriod;

    private final Set<String> keyResolvers; // IP, USER_ID, TENANT_ID

    public RateLimitPolicy(String id,
                           Strategy strategy,
                           long capacity,
                           long refillTokens,
                           Duration refillPeriod,
                           Set<String> keyResolvers) {
        this.id = Objects.requireNonNull(id);
        this.strategy = Objects.requireNonNull(strategy);
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriod = Objects.requireNonNull(refillPeriod);
        this.keyResolvers = Set.copyOf(keyResolvers);
    }

    public String id() { return id; }
    public Strategy strategy() { return strategy; }
    public long capacity() { return capacity; }
    public long refillTokens() { return refillTokens; }
    public Duration refillPeriod() { return refillPeriod; }
    public Set<String> keyResolvers() { return keyResolvers; }

}
