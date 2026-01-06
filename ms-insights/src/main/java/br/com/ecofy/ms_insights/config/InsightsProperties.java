package br.com.ecofy.ms_insights.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ecofy.insights")
public record InsightsProperties (

        @Valid Topics topics,
        @Valid Idempotency idempotency,
        @Valid Engine engine
) {

    public record Topics(
            @NotBlank String categorizedTransactionTopic,
            @NotBlank String budgetAlertTopic,
            @NotBlank String insightCreatedTopic,
            // opcional (pode ficar vazio/null se você não publicar relatórios)
            String reportReadyTopic
    ) { }

    public record Idempotency(
            @Min(1) @Max(604800) // 1s .. 7d (ajuste se quiser)
            int ttlSeconds
    ) { }

    public record Engine(
            @Min(10) @Max(20000)
            int maxTransactionsToAnalyze,

            @Min(0) @Max(100)
            int minScoreToPublish,

            boolean publishReports
    ) { }
}
