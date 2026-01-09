package br.com.ecofy.ms_notification.core.application.service;

import br.com.ecofy.ms_notification.config.NotificationProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RetryPolicyService {

    private final NotificationProperties props;

    public RetryPolicyService(NotificationProperties props) {
        this.props = props;
    }

    public boolean canRetry(int attemptCount) {
        return attemptCount < props.getRetry().getMaxAttempts();
    }

    public Duration computeBackoff(int attemptCount) {
        // attemptCount starts at 0..n
        double multiplier = Math.pow(props.getRetry().getMultiplier(), Math.max(0, attemptCount));
        long ms = (long) (props.getRetry().getBaseBackoff().toMillis() * multiplier);
        return Duration.ofMillis(Math.min(ms, 60_000)); // clamp 60s
    }
}