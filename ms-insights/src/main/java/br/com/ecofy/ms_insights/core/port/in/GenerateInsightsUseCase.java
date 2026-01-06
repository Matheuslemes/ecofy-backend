package br.com.ecofy.ms_insights.core.port.in;

import br.com.ecofy.ms_insights.core.application.command.GenerateInsightsCommand;
import br.com.ecofy.ms_insights.core.application.result.InsightsBundleResult;

public interface GenerateInsightsUseCase {
    InsightsBundleResult generate(GenerateInsightsCommand cmd);
}
