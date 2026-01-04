package br.com.ecofy.ms_budgeting.core.domain.exception;

public class IdempotencyViolationException extends RuntimeException {

    public IdempotencyViolationException(String key) {
        super("Idempotency key already used: " + key);
    }

}