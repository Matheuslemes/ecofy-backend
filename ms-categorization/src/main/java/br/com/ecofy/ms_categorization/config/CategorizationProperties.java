package br.com.ecofy.ms_categorization.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ecofy.categorization")
@Getter
@Setter
@ToString
public class CategorizationProperties {

    private Topics topics = new Topics();
    private Idempotency idempotency = new Idempotency();
    private RuleEngine ruleEngine = new RuleEngine();

    @Getter
    @Setter
    @ToString
    public static class Topics {
        private String categorizationRequest = "categorization.request";
        private String transactionCategorized = "categorization.transaction-categorized";
        private String categorizationApplied = "categorization.applied";
    }

    @Getter
    @Setter
    @ToString
    public static class Idempotency {
        private long ttlSeconds = 3600;
    }

    @Getter
    @Setter
    @ToString
    public static class RuleEngine {
        private int maxRulesToEvaluate = 200;
        private boolean bestScoreWins = true;
        private int minScoreToCategorize = 1;
        private boolean createSuggestionWhenUnmatched = true;
    }
}
