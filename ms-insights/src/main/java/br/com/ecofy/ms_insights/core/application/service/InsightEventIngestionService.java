package br.com.ecofy.ms_insights.core.application.service;

import br.com.ecofy.ms_insights.core.application.command.GenerateInsightsCommand;
import br.com.ecofy.ms_insights.core.domain.enums.PeriodGranularity;
import br.com.ecofy.ms_insights.core.port.in.GenerateInsightsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class InsightEventIngestionService {

    private final GenerateInsightsUseCase generateInsightsUseCase;
    private final Clock clock;

    public InsightEventIngestionService(GenerateInsightsUseCase generateInsightsUseCase, Clock clock) {
        this.generateInsightsUseCase = Objects.requireNonNull(generateInsightsUseCase, "generateInsightsUseCase must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public void onSignalGenerate(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        // Estratégia simples e determinística: mês corrente (start = 1º dia; end = hoje no clock)
        LocalDate today = LocalDate.now(clock);
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today;

        String idempotencyKey = buildIdempotencyKey(userId, start, end, PeriodGranularity.MONTH);

        log.info("[InsightEventIngestionService] - [onSignalGenerate] -> userId={} granularity={} start={} end={} idempotencyKey={}",
                userId, PeriodGranularity.MONTH, start, end, idempotencyKey);

        generateInsightsUseCase.generate(new GenerateInsightsCommand(
                userId,
                start,
                end,
                PeriodGranularity.MONTH,
                idempotencyKey
        ));
    }

    private static String buildIdempotencyKey(UUID userId, LocalDate start, LocalDate end, PeriodGranularity g) {
        // Curta, estável e legível; evita variar com timestamp e reduz risco de estouro de header.
        // Ex.: ins-kafka|<user>|MONTH|2026-01-01|2026-01-06
        return "ins-kafka|" + userId + "|" + g + "|" + start + "|" + end;
    }
}
