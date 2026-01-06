package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.Insight;

public interface PublishInsightCreatedEventPort {
    void publish(Insight insight);
}
