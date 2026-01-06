package br.com.ecofy.ms_insights.core.port.in;

import br.com.ecofy.ms_insights.core.application.command.RebuildInsightsCommand;

public interface RebuildInsightsUseCase {
    void rebuild(RebuildInsightsCommand cmd);
}
