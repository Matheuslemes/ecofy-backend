package br.com.ecofy.ms_insights.core.port.in;

import br.com.ecofy.ms_insights.core.application.result.InsightsBundleResult;

import java.util.UUID;

public interface GetDashboardInsightsUseCase {
    InsightsBundleResult getDashboard(UUID userId);
}
